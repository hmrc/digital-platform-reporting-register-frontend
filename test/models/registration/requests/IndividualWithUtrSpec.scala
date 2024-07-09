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

package models.registration.requests

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class IndividualWithUtrSpec extends AnyFreeSpec with Matchers {

  "individual with UTR" - {

    "must serialise with no details" in {

      val individual = IndividualWithUtr("123", None)
      val json = Json.toJson(individual)

      json mustEqual Json.obj(
        "type" -> "individual",
        "utr" -> "123"
      )
    }

    "must serialise with details" in {

      val individual = IndividualWithUtr("123", Some(IndividualDetails("first", "last")))
      val json = Json.toJson(individual)

      json mustEqual Json.obj(
        "type" -> "individual",
        "utr" -> "123",
        "details" -> Json.obj(
          "firstName" -> "first",
          "lastName" -> "last"
        )
      )
    }
  }
}
