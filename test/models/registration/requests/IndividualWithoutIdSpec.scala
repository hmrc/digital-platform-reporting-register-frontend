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

import cats.data.*
import models.registration.Address
import models.{Country, IndividualName, InternationalAddress, UkAddress, UserAnswers}
import org.scalatest.{EitherValues, OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.*

import java.time.LocalDate

class IndividualWithoutIdSpec
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with EitherValues {

  "individual without Id" - {

    val aName = IndividualName("first", "last")
    val aDateOfBirth = LocalDate.of(2000, 1, 2)
    val aUkCountry = Country.ukCountries.head
    val anInternationalCountry = Country.internationalCountries.head
    val aUkAddress = UkAddress("line 1", Some("line 2"), "town", Some("county"), "postcode", aUkCountry)
    val anInternationalAddress = InternationalAddress("line 1", Some("line 2"), "city", Some("region"), Some("postcode"), anInternationalCountry)

    "must build from user answers when questions have been answered with a UK address" in {

      val answers =
        UserAnswers("id", None)
          .set(IndividualNamePage, aName).success.value
          .set(DateOfBirthPage, aDateOfBirth).success.value
          .set(AddressInUkPage, true).success.value
          .set(UkAddressPage, aUkAddress).success.value

      val result = IndividualWithoutId.build(answers)
      val expectedAddress = Address("line 1", Some("line 2"), Some("town"), Some("county"), Some("postcode"), aUkCountry.code)
      result.value mustEqual IndividualWithoutId("first", "last", aDateOfBirth, expectedAddress)
    }

    "must build from user answers when questions have been answered with an international address" in {

      val answers =
        UserAnswers("id", None)
          .set(IndividualNamePage, aName).success.value
          .set(DateOfBirthPage, aDateOfBirth).success.value
          .set(AddressInUkPage, false).success.value
          .set(InternationalAddressPage, anInternationalAddress).success.value

      val result = IndividualWithoutId.build(answers)
      val expectedAddress = Address("line 1", Some("line 2"), Some("city"), Some("region"), Some("postcode"), anInternationalCountry.code)
      result.value mustEqual IndividualWithoutId("first", "last", aDateOfBirth, expectedAddress)
    }

    "must fail to build from user answers and report all errors when mandatory data is missing" in {

      val answers = UserAnswers("id", None)

      val result = IndividualWithoutId.build(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(IndividualNamePage, DateOfBirthPage, AddressInUkPage)
    }

    "must fail to build from user answers and report all errors when UK address is missing" in {

      val answers =
        UserAnswers("id", None)
          .set(IndividualNamePage, aName).success.value
          .set(DateOfBirthPage, aDateOfBirth).success.value
          .set(AddressInUkPage, true).success.value

      val result = IndividualWithoutId.build(answers)
      result.left.value.toChain.toList must contain only UkAddressPage
    }

    "must fail to build from user answers and report all errors when international address is missing" in {

      val answers =
        UserAnswers("id", None)
          .set(IndividualNamePage, aName).success.value
          .set(DateOfBirthPage, aDateOfBirth).success.value
          .set(AddressInUkPage, false).success.value

      val result = IndividualWithoutId.build(answers)
      result.left.value.toChain.toList must contain only InternationalAddressPage
    }
  }
}
