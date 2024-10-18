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

package models.subscription.responses

import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}

import java.time.Instant


sealed trait SubscriptionResponse

object SubscriptionResponse {

  implicit lazy val reads: Reads[SubscriptionResponse] =
    SubscribedResponse.format.widen or AlreadySubscribedResponse.reads.widen

  implicit lazy val writes: OWrites[SubscriptionResponse] = new OWrites[SubscriptionResponse] {
    override def writes(o: SubscriptionResponse): JsObject = o match {
      case x: SubscribedResponse => Json.toJsObject(x)(SubscribedResponse.format)
      case x: AlreadySubscribedResponse => Json.toJsObject(x)(AlreadySubscribedResponse.writes)
    }
  }

  def encryptedFormat(implicit crypto: Encrypter & Decrypter): OFormat[SubscriptionResponse] = {

    implicit val sensitiveFormat: Format[SensitiveString] =
      JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)

    val encryptedReads: Reads[SubscriptionResponse] =
      (__ \ "encrypted").read[SensitiveString].map(x => Json.parse(x.decryptedValue).as[SubscriptionResponse])

    val encryptedWrites: OWrites[SubscriptionResponse] = new OWrites[SubscriptionResponse] {
      override def writes(o: SubscriptionResponse): JsObject =
        Json.obj(
          "encrypted" -> SensitiveString(Json.stringify(Json.toJsObject(o)))
        )
    }

    OFormat(encryptedReads, encryptedWrites)
  }
}

final case class SubscribedResponse(dprsId: String, subscribedDateTime: Instant) extends SubscriptionResponse

object SubscribedResponse {
  implicit lazy val format: OFormat[SubscribedResponse] = Json.format
}

final case class AlreadySubscribedResponse() extends SubscriptionResponse

object AlreadySubscribedResponse {

  implicit lazy val writes: OWrites[AlreadySubscribedResponse] = new OWrites[AlreadySubscribedResponse] {

    override def writes(o: AlreadySubscribedResponse): JsObject =
      Json.obj("result" -> "alreadySubscribed")
  }

  implicit lazy val reads: Reads[AlreadySubscribedResponse] =
    (__ \ "result")
      .read[String]
      .flatMap { t =>
        if (t == "alreadySubscribed") {
          Reads.pure(AlreadySubscribedResponse())
        } else {
          Reads.failed("could not read an already subscribed result")
        }
      }
}

