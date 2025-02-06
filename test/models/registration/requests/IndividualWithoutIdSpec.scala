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
import builders.ContactDetailsBuilder.aContactDetails
import builders.JerseyGuernseyIoMAddressBuilder.aJerseyGuernseyIsleOfManAddress
import builders.UserBuilder.aUser
import cats.data.*
import models.registration.{Address, RegisteredAddressCountry}
import models.{Country, DefaultCountriesList, IndividualName, InternationalAddress, UkAddress, UserAnswers}
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages.*

import java.time.LocalDate

class IndividualWithoutIdSpec extends SpecBase
  with TryValues
  with OptionValues
  with EitherValues {

  "individual without Id" - {

    val countriesList = new DefaultCountriesList()
    val aName = IndividualName("first", "last")
    val aDateOfBirth = LocalDate.of(2000, 1, 2)
    val aUkCountry = Country.UnitedKingdom
    val anInternationalCountry = countriesList.internationalCountries.head
    val aUkAddress = UkAddress("line 1", Some("line 2"), "town", Some("county"), "postcode", aUkCountry)
    val anInternationalAddress = InternationalAddress("line 1", Some("line 2"), "city", Some("region"), "postcode", anInternationalCountry)

    "must build from user answers when questions have been answered with a UK address" in {
      val answers = UserAnswers(aUser)
        .set(IndividualNamePage, aName).success.value
        .set(DateOfBirthPage, aDateOfBirth).success.value
        .set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
        .set(UkAddressPage, aUkAddress).success.value
        .set(IndividualEmailAddressPage, aContactDetails.emailAddress).success.value
        .set(CanPhoneIndividualPage, false).success.value

      val result = IndividualWithoutId.build(answers)
      val expectedAddress = Address("line 1", Some("line 2"), Some("town"), Some("county"), Some("postcode"), aUkCountry.code)
      result.value mustEqual IndividualWithoutId("first", "last", aDateOfBirth, expectedAddress, aContactDetails)
    }

    "must build from user answers when questions have been answered with an international address" in {

      val answers = UserAnswers(aUser)
        .set(IndividualNamePage, aName).success.value
        .set(DateOfBirthPage, aDateOfBirth).success.value
        .set(AddressInUkPage, RegisteredAddressCountry.International).success.value
        .set(InternationalAddressPage, anInternationalAddress).success.value
        .set(IndividualEmailAddressPage, aContactDetails.emailAddress).success.value
        .set(CanPhoneIndividualPage, false).success.value

      val result = IndividualWithoutId.build(answers)
      val expectedAddress = Address("line 1", Some("line 2"), Some("city"), Some("region"), Some("postcode"), anInternationalCountry.code)
      result.value mustEqual IndividualWithoutId("first", "last", aDateOfBirth, expectedAddress, aContactDetails)
    }

    "must build from user answers when questions have been answered with a JerseyGuernseyIoM address" in {
      val answers = UserAnswers(aUser)
        .set(IndividualNamePage, aName).success.value
        .set(DateOfBirthPage, aDateOfBirth).success.value
        .set(AddressInUkPage, RegisteredAddressCountry.JerseyGuernseyIsleOfMan).success.value
        .set(JerseyGuernseyIoMAddressPage, aJerseyGuernseyIsleOfManAddress).success.value
        .set(IndividualEmailAddressPage, aContactDetails.emailAddress).success.value
        .set(CanPhoneIndividualPage, false).success.value

      val result = IndividualWithoutId.build(answers)
      val expectedAddress = Address("Address line 1", Some("Address line 2"), Some("default-city"), Some("default-region"), Some("default-postal-code"), "GG")
      result.value mustEqual IndividualWithoutId("first", "last", aDateOfBirth, expectedAddress, aContactDetails)
    }

    "must fail to build from user answers and report all errors when mandatory data is missing" in {
      val answers = UserAnswers(aUser)
      val result = IndividualWithoutId.build(answers)

      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        IndividualNamePage,
        DateOfBirthPage,
        AddressInUkPage,
        IndividualEmailAddressPage,
        CanPhoneIndividualPage
      )
    }

    "must fail to build from user answers and report all errors when UK address is missing" in {
      val answers = UserAnswers(aUser)
        .set(IndividualNamePage, aName).success.value
        .set(DateOfBirthPage, aDateOfBirth).success.value
        .set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value

      val result = IndividualWithoutId.build(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        UkAddressPage,
        IndividualEmailAddressPage,
        CanPhoneIndividualPage
      )
    }

    "must fail to build from user answers and report all errors when international address is missing" in {
      val answers = UserAnswers(aUser)
        .set(IndividualNamePage, aName).success.value
        .set(DateOfBirthPage, aDateOfBirth).success.value
        .set(AddressInUkPage, RegisteredAddressCountry.International).success.value

      val result = IndividualWithoutId.build(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        InternationalAddressPage,
        IndividualEmailAddressPage,
        CanPhoneIndividualPage
      )
    }

    "must fail to build from user answers and report all errors when JerseyGuernseyIoM address is missing" in {
      val answers = UserAnswers(aUser)
        .set(IndividualNamePage, aName).success.value
        .set(DateOfBirthPage, aDateOfBirth).success.value
        .set(AddressInUkPage, RegisteredAddressCountry.JerseyGuernseyIsleOfMan).success.value

      val result = IndividualWithoutId.build(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        JerseyGuernseyIoMAddressPage,
        IndividualEmailAddressPage,
        CanPhoneIndividualPage
      )
    }
  }
}
