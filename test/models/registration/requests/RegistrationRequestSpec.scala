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

import builders.AddressBuilder.anAddress
import builders.ContactDetailsBuilder.aContactDetails
import models.BusinessType
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class RegistrationRequestSpec extends AnyFreeSpec with Matchers {

  "a registration request" - {
    "must write an individual with NINO request" in {
      val dob = LocalDate.of(2000, 1, 2)
      val request: RegistrationRequest = IndividualWithNino("nino", IndividualDetails("first", "last"), dob)
      val json = Json.toJson(request)

      json mustEqual Json.obj(
        "nino" -> "nino",
        "firstName" -> "first",
        "lastName" -> "last",
        "dateOfBirth" -> "2000-01-02"
      )
    }

    "must write an individual with UTR request" in {
      val request: RegistrationRequest = IndividualWithUtr("123", IndividualDetails("first", "last"))
      val json = Json.toJson(request)

      json mustEqual Json.obj(
        "type" -> "individual",
        "utr" -> "123",
        "details" -> Json.obj(
          "firstName" -> "first",
          "lastName" -> "last"
        )
      )
    }

    "must write an organisation with UTR request" in {
      val request: RegistrationRequest = OrganisationWithUtr("123", Some(OrganisationDetails("name", BusinessType.LimitedCompany)))
      val json = Json.toJson(request)

      json mustEqual Json.obj(
        "type" -> "organisation",
        "utr" -> "123",
        "details" -> Json.obj(
          "name" -> "name",
          "organisationType" -> "limitedCompany"
        )
      )
    }

    "must write an individual" in {
      val contactDetails = aContactDetails.copy(emailAddress = "some.email@example.com", phoneNumber = Some("1234"))
      val request: RegistrationRequest = IndividualWithoutId("first", "last", LocalDate.of(2000, 1, 2), anAddress, contactDetails)
      val json = Json.toJson(request)

      json mustEqual Json.obj(
        "firstName" -> "first",
        "lastName" -> "last",
        "dateOfBirth" -> "2000-01-02",
        "address" -> Json.obj(
          "addressLine1" -> anAddress.addressLine1,
          "addressLine2" -> anAddress.addressLine2,
          "addressLine3" -> anAddress.addressLine3,
          "addressLine4" -> anAddress.addressLine4,
          "postalCode" -> anAddress.postalCode,
          "countryCode" -> anAddress.countryCode
        ),
        "contactDetails" -> Json.obj(
          "emailAddress" -> "some.email@example.com",
          "phoneNumber" -> "1234"
        )
      )
    }

    "must write an organisation" in {
      val contactDetails = aContactDetails.copy(emailAddress = "some.email@example.com", phoneNumber = None)
      val request: RegistrationRequest = OrganisationWithoutId("name", anAddress, contactDetails)
      val json = Json.toJson(request)

      json mustEqual Json.obj(
        "name" -> "name",
        "address" -> Json.obj(
          "addressLine1" -> anAddress.addressLine1,
          "addressLine2" -> anAddress.addressLine2,
          "addressLine3" -> anAddress.addressLine3,
          "addressLine4" -> anAddress.addressLine4,
          "postalCode" -> anAddress.postalCode,
          "countryCode" -> anAddress.countryCode
        ),
        "contactDetails" -> Json.obj(
          "emailAddress" -> "some.email@example.com"
        )
      )
    }
  }
}
