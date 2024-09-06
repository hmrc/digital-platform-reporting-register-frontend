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

package models

import cats.implicits.*
import models.BusinessType.{Individual, SoleTrader}
import models.registration.responses.{MatchResponseWithId, MatchResponseWithoutId}
import models.subscription.requests.SubscriptionRequest
import models.subscription.responses.SubscriptionResponse
import pages.{BusinessNameNoUtrPage, BusinessTypePage, RegistrationTypePage}
import play.api.libs.json.*
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}

final case class SubscriptionDetails(subscriptionResponse: SubscriptionResponse,
                                     subscriptionRequest: SubscriptionRequest,
                                     registrationType: RegistrationType,
                                     businessType: Option[BusinessType],
                                     businessName: Option[String])

object SubscriptionDetails {

  implicit lazy val format: OFormat[SubscriptionDetails] = Json.format

  def apply(response: SubscriptionResponse, request: SubscriptionRequest, userAnswers: UserAnswers): SubscriptionDetails = {
    val registrationType = userAnswers.get(RegistrationTypePage).getOrElse(RegistrationType.ThirdParty)
    val businessName = getBusinessName(userAnswers)

    SubscriptionDetails(
      subscriptionResponse = response,
      subscriptionRequest = request,
      registrationType = registrationType,
      businessType = userAnswers.get(BusinessTypePage),
      businessName = businessName
    )
  }

  private def getBusinessName(userAnswers: UserAnswers): Option[String] =
    userAnswers.get(BusinessTypePage).flatMap {
      case Individual | SoleTrader => None
      case _ => userAnswers.registrationResponse.flatMap {
        case x: MatchResponseWithId => x.organisationName
        case x: MatchResponseWithoutId => userAnswers.get(BusinessNameNoUtrPage)
        case _ => None
      }
    }.orElse {
      userAnswers.registrationResponse.flatMap {
        case x: MatchResponseWithId => x.organisationName
        case _ => None
      }
    }

  def encryptedFormat(implicit crypto: Encrypter with Decrypter): OFormat[SubscriptionDetails] = {

    implicit val sensitiveFormat: Format[SensitiveString] =
      JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)

    val encryptedReads: Reads[SubscriptionDetails] =
      (__ \ "encrypted").read[SensitiveString].map(x => Json.parse(x.decryptedValue).as[SubscriptionDetails](format))

    val encryptedWrites: OWrites[SubscriptionDetails] = new OWrites[SubscriptionDetails] {
      override def writes(o: SubscriptionDetails): JsObject =
        Json.obj(
          "encrypted" -> SensitiveString(Json.stringify(Json.toJsObject(o)(format)))
        )
    }

    OFormat(encryptedReads, encryptedWrites)
  }
}
