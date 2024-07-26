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

package models.registration.responses

import models.registration.Address
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class RegistrationResponseSpec extends AnyFreeSpec with Matchers {

  "registration response" - {

    "must serialise / deserialise" - {

      "a match with Id" in {

        val response = MatchResponseWithId("safeId", Address("line 1", None, None, None, None, "GB"), None)
        val json = Json.toJson(response)
        val result = json.as[RegistrationResponse]
        result mustEqual response
      }

      "a match without Id" in {

        val response = MatchResponseWithoutId("safeId")
        val json = Json.toJson(response)
        val result = json.as[RegistrationResponse]
        result mustEqual response
      }

      "a `no match`" in {

        val response = NoMatchResponse()
        val json = Json.toJson(response)
        val result = json.as[RegistrationResponse]
        result mustEqual response
      }

      "an `already subscribed`" in {

        val response = AlreadySubscribedResponse()
        val json = Json.toJson(response)
        val result = json.as[RegistrationResponse]
        result mustEqual response
      }
    }
  }
}
