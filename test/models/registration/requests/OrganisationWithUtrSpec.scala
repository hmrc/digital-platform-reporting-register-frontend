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

import models.BusinessType
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class OrganisationWithUtrSpec extends AnyFreeSpec with Matchers {

  "organisation with UTR" - {

    "must serialise with no details" in {

      val organisation = OrganisationWithUtr("123", None)
      val json = Json.toJson(organisation)

      json mustEqual Json.obj(
        "type" -> "organisation",
        "utr" -> "123"
      )
    }

    "must serialise with details" in {

      val organisation = OrganisationWithUtr("123", Some(OrganisationDetails("name", BusinessType.LimitedCompany)))
      val json = Json.toJson(organisation)

      json mustEqual Json.obj(
        "type" -> "organisation",
        "utr" -> "123",
        "details" -> Json.obj(
          "name" -> "name",
          "organisationType" -> "limitedCompany"
        )
      )
    }
  }
}
