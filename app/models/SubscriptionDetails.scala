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
import models.subscription.requests.SubscriptionRequest
import models.subscription.responses.SubscriptionResponse
import play.api.libs.json.*
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}

final case class SubscriptionDetails(subscriptionResponse: SubscriptionResponse,
                                     subscriptionRequest: SubscriptionRequest,
                                     registrationType: RegistrationType)

object SubscriptionDetails {

  implicit lazy val format: OFormat[SubscriptionDetails] = Json.format

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
