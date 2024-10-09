/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import config.AppConfig
import connectors.UserAllowListConnector
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class UserAllowListService @Inject()(connector: UserAllowListConnector, appConfig: AppConfig) {

  def isUserAllowed(enrolments: Enrolments)(implicit hc: HeaderCarrier): Future[Boolean] =
    if (appConfig.userAllowListEnabled) {
      getCtUtrEnrolment(enrolments).map { utr =>
        connector.check(appConfig.utrAllowListFeature, utr)
      }.getOrElse {
        getVatEnrolment(enrolments).map { vrn =>
          connector.check(appConfig.vrnAllowListFeature, vrn)
        }.getOrElse(Future.successful(false))
      }
    } else {
      Future.successful(true)
    }

  private def getCtUtrEnrolment(enrolments: Enrolments): Option[String] =
    enrolments.getEnrolment("IR-CT")
      .flatMap { enrolment =>
        enrolment.identifiers
          .find(_.key == "UTR")
          .map(_.value)
      }

  private def getVatEnrolment(enrolments: Enrolments): Option[String] =
    enrolments.getEnrolment("HMRC-MTD-VAT")
      .flatMap { enrolment =>
        enrolment.identifiers
          .find(_.key == "VRN")
          .map(_.value)
      }.orElse {
        enrolments.getEnrolment("HMCE-VATDEC-ORG")
          .flatMap { enrolment =>
            enrolment.identifiers
              .find(_.key == "VATRegNo")
              .map(_.value)
          }
      }
}
