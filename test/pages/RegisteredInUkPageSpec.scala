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

import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.{CheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}

class RegisteredInUkPageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to UTR when the answer is yes" in {
        val answers = anEmptyAnswer.set(RegisteredInUkPage, true).success.value
        RegisteredInUkPage.nextPage(NormalMode, answers) mustEqual routes.UtrController.onPageLoad(NormalMode)
      }

      "must go to Has UTR when the answer is no" in {
        val answers = anEmptyAnswer.set(RegisteredInUkPage, false).success.value
        RegisteredInUkPage.nextPage(NormalMode, answers) mustEqual routes.HasUtrController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        RegisteredInUkPage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
