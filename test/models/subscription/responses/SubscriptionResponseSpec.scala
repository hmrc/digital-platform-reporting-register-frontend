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

import base.SpecBase
import play.api.libs.json.Json

import java.time.Instant

class SubscriptionResponseSpec extends SpecBase {

  "subscription response" - {
    "must serialise / deserialise a subscribed response" in {
      val response = SubscribedResponse("DPRS123", Instant.now())
      val json = Json.toJson[SubscriptionResponse](response)
      val result = json.as[SubscriptionResponse]
      result mustEqual response
    }

    "must serialise / deserialise an already subscribed response" in {
      val response = AlreadySubscribedResponse()
      val json = Json.toJson[SubscriptionResponse](response)
      val result = json.as[SubscriptionResponse]
      result mustEqual response
    }
  }
}
