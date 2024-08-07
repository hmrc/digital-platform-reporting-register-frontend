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

package pages

import controllers.routes
import models.{CheckMode, IndividualName, NormalMode, UserAnswers}
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class IndividualNamePageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id", None)

    "in Normal Mode" - {

      "must go to Date Of Birth page if there is an answer" in {

        val answers = emptyAnswers.set(IndividualNamePage, IndividualName("", "")).success.value

        IndividualNamePage.nextPage(NormalMode, answers) mustEqual routes.DateOfBirthController.onPageLoad(NormalMode)
      }

      "must go to error page if there is no data" in {

        IndividualNamePage.nextPage(NormalMode, emptyAnswers) mustEqual routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        IndividualNamePage.nextPage(CheckMode, emptyAnswers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
