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

import builders.IndividualWithoutIdBuilder.anIndividualWithoutId
import builders.InternationalAddressBuilder.anInternationalAddress
import builders.UkAddressBuilder.aUkAddress
import builders.UserAnswersBuilder.aUserAnswers
import models.registration.Address
import models.{IndividualName, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages.*
import play.api.libs.json.Json

import java.time.LocalDate

class IndividualWithoutIdSpec extends AnyFreeSpec
  with Matchers
  with TryValues
  with OptionValues
  with EitherValues {

  "Individual Without Id" - {
    "must serialise" in {
      Json.toJson(anIndividualWithoutId) mustEqual Json.obj(
        "firstName" -> anIndividualWithoutId.firstName,
        "lastName" -> anIndividualWithoutId.lastName,
        "dateOfBirth" -> anIndividualWithoutId.dateOfBirth.toString,
        "address" -> Json.toJson(anIndividualWithoutId.address)
      )
    }

    "must build from user answers when name, dob and non uk address have been answered" in {
      val answers = aUserAnswers.set(IndividualNamePage, IndividualName("first-name", "last-name")).success.value
        .set(DateOfBirthPage, LocalDate.of(2000, 1, 1)).success.value
        .set(AddressInUkPage, false).success.value
        .set(InternationalAddressPage, anInternationalAddress).success.value

      IndividualWithoutId.build(answers).value mustEqual
        IndividualWithoutId("first-name", "last-name", LocalDate.of(2000, 1, 1), Address(anInternationalAddress))
    }

    "must build from user answers when name, dob and uk address have been answered" in {
      val answers = aUserAnswers.set(IndividualNamePage, IndividualName("first-name", "last-name")).success.value
        .set(DateOfBirthPage, LocalDate.of(2000, 1, 1)).success.value
        .set(AddressInUkPage, true).success.value
        .set(UkAddressPage, aUkAddress).success.value

      IndividualWithoutId.build(answers).value mustEqual
        IndividualWithoutId("first-name", "last-name", LocalDate.of(2000, 1, 1), Address(aUkAddress))
    }

    "must fail to build from user answers and report all errors when mandatory data is missing" in {
      val result = IndividualWithoutId.build(UserAnswers("id", None))

      result.left.value.toChain.toList must contain theSameElementsAs Seq(IndividualNamePage, IndividualNamePage, DateOfBirthPage, InternationalAddressPage)
    }
  }
}
