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
}

final case class SubscribedResponse(dprsId: String) extends SubscriptionResponse

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

