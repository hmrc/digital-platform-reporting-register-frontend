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

import base.SpecBase
import builders.AddressBuilder.anAddress
import builders.BusinessAddressBuilder.aBusinessAddress
import builders.ContactDetailsBuilder.aContactDetails
import builders.UkAddressBuilder.aUkAddress
import builders.UserAnswersBuilder.anEmptyAnswer
import cats.data.NonEmptyChain
import models.registration.requests.RegistrationRequest.BuildRegistrationRequestFailure
import models.registration.{Address, RegisteredAddressCountry}
import models.{BusinessType, IndividualName}
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages.*
import play.api.libs.json.Json

import java.time.LocalDate

class RegistrationRequestSpec extends SpecBase
  with TryValues
  with OptionValues
  with EitherValues {

  private val underTest = RegistrationRequest

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

  "build(...)" - {
    "must create IndividualWithoutId when data available and SoleTrader" in {
      val userAnswers = anEmptyAnswer
        .set(BusinessTypePage, BusinessType.SoleTrader).success.value
        .set(IndividualNamePage, IndividualName("some-first-name", "some-last-name")).success.value
        .set(DateOfBirthPage, LocalDate.parse("2000-01-01")).success.value
        .set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
        .set(UkAddressPage, aUkAddress).success.value
        .set(IndividualEmailAddressPage, "some.email@example.com").success.value
        .set(CanPhoneIndividualPage, false).success.value

      underTest.build(userAnswers).value mustBe IndividualWithoutId(
        firstName = "some-first-name",
        lastName = "some-last-name",
        dateOfBirth = LocalDate.parse("2000-01-01"),
        address = Address(aUkAddress),
        contactDetails = ContactDetails("some.email@example.com", None)
      )
    }

    "must create IndividualWithoutId when data available and Individual" in {
      val userAnswers = anEmptyAnswer
        .set(BusinessTypePage, BusinessType.Individual).success.value
        .set(IndividualNamePage, IndividualName("some-first-name", "some-last-name")).success.value
        .set(DateOfBirthPage, LocalDate.parse("2000-01-01")).success.value
        .set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
        .set(UkAddressPage, aUkAddress).success.value
        .set(IndividualEmailAddressPage, "some.email@example.com").success.value
        .set(CanPhoneIndividualPage, false).success.value

      underTest.build(userAnswers).value mustBe IndividualWithoutId(
        firstName = "some-first-name",
        lastName = "some-last-name",
        dateOfBirth = LocalDate.parse("2000-01-01"),
        address = Address(aUkAddress),
        contactDetails = ContactDetails("some.email@example.com", None)
      )
    }

    "must create OrganisationWithoutId when data available and not Individual and not SoleTrader" in {
      val userAnswers = anEmptyAnswer
        .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
        .set(BusinessNameNoUtrPage, "some-business-name").success.value
        .set(BusinessAddressPage, aBusinessAddress).success.value
        .set(PrimaryContactNamePage, "some-contact-name").success.value
        .set(PrimaryContactEmailAddressPage, "some.email@example.com").success.value
        .set(CanPhonePrimaryContactPage, false).success.value

      underTest.build(userAnswers).value mustBe OrganisationWithoutId(
        name = "some-business-name",
        address = Address(aBusinessAddress),
        contactDetails = ContactDetails("some.email@example.com", None)
      )
    }

    "should return errors when" - {
      "BusinessTypePage not provided" in {
        val userAnswers = anEmptyAnswer
          .remove(BusinessTypePage).success.value

        val result = underTest.build(userAnswers)
        result.left.value.toChain.toList must contain theSameElementsAs Seq(BusinessTypePage)
      }

      "IndividualWithoutId cannot be created from given user answers" in {
        val userAnswers = anEmptyAnswer
          .set(BusinessTypePage, BusinessType.Individual).success.value
          .remove(IndividualNamePage).success.value
          .remove(DateOfBirthPage).success.value
          .remove(AddressInUkPage).success.value
          .remove(UkAddressPage).success.value
          .remove(IndividualEmailAddressPage).success.value
          .remove(CanPhoneIndividualPage).success.value

        val result = underTest.build(userAnswers)
        result.left.value.toChain.toList must contain theSameElementsAs Seq(
          IndividualNamePage,
          DateOfBirthPage,
          AddressInUkPage,
          IndividualEmailAddressPage,
          CanPhoneIndividualPage
        )
      }

      "OrganisationWithoutId cannot be created from given user answers" in {
        val userAnswers = anEmptyAnswer
          .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
          .remove(BusinessNameNoUtrPage).success.value
          .remove(BusinessAddressPage).success.value
          .remove(PrimaryContactNamePage).success.value
          .remove(PrimaryContactEmailAddressPage).success.value
          .remove(CanPhonePrimaryContactPage).success.value

        val result = underTest.build(userAnswers)
        result.left.value.toChain.toList must contain theSameElementsAs Seq(
          BusinessNameNoUtrPage,
          BusinessAddressPage,
          PrimaryContactNamePage,
          PrimaryContactEmailAddressPage,
          CanPhonePrimaryContactPage
        )
      }
    }
  }

  "BuildRegistrationRequestFailure" - {
    "must contain correct message" in {
      val errors = NonEmptyChain(AddressInUkPage, BusinessAddressPage)
      val underTest = BuildRegistrationRequestFailure(errors)
      underTest.getMessage mustBe s"Unable to build a registration request, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}"
    }
  }
}
