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

import base.SpecBase
import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.{CheckMode, IndividualName, NormalMode}
import org.scalatest.{OptionValues, TryValues}

class IndividualNamePageSpec extends SpecBase with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Date Of Birth page if there is an answer" in {
        val answers = anEmptyAnswer.set(IndividualNamePage, IndividualName("", "")).success.value

        IndividualNamePage.nextPage(NormalMode, answers) mustEqual routes.DateOfBirthController.onPageLoad(NormalMode)
      }

      "must go to error page if there is no data" in {
        IndividualNamePage.nextPage(NormalMode, anEmptyAnswer) mustEqual routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        IndividualNamePage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
