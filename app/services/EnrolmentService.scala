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

import connectors.{PendingEnrolmentConnector, TaxEnrolmentConnector}
import models.eacd.EnrolmentDetails
import models.eacd.requests.{GroupEnrolment, UpsertKnownFacts}
import models.enrolment.requests.PendingEnrolmentRequest
import org.apache.pekko.Done
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentService @Inject()(taxEnrollmentConnector: TaxEnrolmentConnector,
                                 pendingEnrolmentConnector: PendingEnrolmentConnector)
                                (implicit ec: ExecutionContext) extends Logging {

  def enrol(enrolmentDetails: EnrolmentDetails)
           (implicit hc: HeaderCarrier): Future[Done] = {
    (for {
      _ <- taxEnrollmentConnector.upsert(UpsertKnownFacts(enrolmentDetails))
      result <- taxEnrollmentConnector.allocateEnrolmentToGroup(GroupEnrolment(enrolmentDetails))
    } yield result).recoverWith {
      case error => pendingEnrolmentConnector.save(PendingEnrolmentRequest(enrolmentDetails))
    }
  }
}
