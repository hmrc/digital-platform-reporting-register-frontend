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
import cats.data.*
import models.{IndividualName, Nino, User, UserAnswers}
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages.{DateOfBirthPage, IndividualNamePage, NinoPage}
import play.api.libs.json.Json

import java.time.LocalDate

class IndividualWithNinoSpec extends SpecBase with TryValues with OptionValues with EitherValues {
      
  private val dateOfBirth = LocalDate.of(2000, 1, 2)

  "IndividualWithNino" - {
    "must serialise" in {
      val individual = IndividualWithNino("nino", IndividualDetails("first", "last"), dateOfBirth)
      val json = Json.toJson(individual)
      
      json mustEqual Json.obj(
        "nino" -> "nino",
        "firstName" -> "first",
        "lastName" -> "last",
        "dateOfBirth" -> "2000-01-02"
      )
    }

    "must build from user answers with NINO present as a tax identifier" in {
      val answers =
        UserAnswers(User("id", taxIdentifier = Some(Nino("nino"))))
          .set(IndividualNamePage, IndividualName("first", "last")).success.value
          .set(DateOfBirthPage, dateOfBirth).success.value

      val result = IndividualWithNino.build(answers)
      result.value mustEqual IndividualWithNino("nino", IndividualDetails("first", "last"), dateOfBirth)
    }

    "must build from user answers with NINO answered by the user" in {
      val answers =
        UserAnswers(User("id"))
          .set(NinoPage, "nino").success.value
          .set(IndividualNamePage, IndividualName("first", "last")).success.value
          .set(DateOfBirthPage, dateOfBirth).success.value

      val result = IndividualWithNino.build(answers)
      result.value mustEqual IndividualWithNino("nino", IndividualDetails("first", "last"), dateOfBirth)
    }
    
    "must fail to build from user answers and report errors when mandatory data is missing" in {
      val answers = UserAnswers(User("id"))
      
      val result = IndividualWithNino.build(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(NinoPage, IndividualNamePage, DateOfBirthPage)
    }
  }
}
