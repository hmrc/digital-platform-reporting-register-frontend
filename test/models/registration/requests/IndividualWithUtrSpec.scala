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
import models.{SoleTraderName, UserAnswers}
import org.scalatest.{EitherValues, OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.{SoleTraderNamePage, UtrPage}
import play.api.libs.json.Json

class IndividualWithUtrSpec
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with EitherValues {

  "individual with UTR" - {

    "must serialise" in {

      val individual = IndividualWithUtr("123", IndividualDetails("first", "last"))
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

    "must build from user answers when UTR and sole trader name have been answered" in {

      val answers =
        UserAnswers("id", None)
          .set(UtrPage, "1234567890").success.value
          .set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value

      val result = IndividualWithUtr.build(answers)
      result.value mustEqual IndividualWithUtr("1234567890", IndividualDetails("first", "last"))
    }

    "must fail to build from user answers and report all errors when mandatory data is missing" in {

      val answers = UserAnswers("id", None)

      val result = IndividualWithUtr.build(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(UtrPage, SoleTraderNamePage)
    }
  }
}
