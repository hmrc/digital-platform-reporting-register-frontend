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
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalatest.TryValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class DateOfBirthPageSpec extends AnyFreeSpec with Matchers with TryValues {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id", None)

    "in Normal Mode" - {

      "must go to Index if NINO supplied" in {

        val answers = emptyAnswers.set(HasNinoPage, true).success.value

        DateOfBirthPage.nextPage(NormalMode, answers) mustEqual routes.IndexController.onPageLoad()
      }

      "must go to Is UK Address if NINO absent" in {

        val answers = emptyAnswers.set(HasNinoPage, false).success.value

        DateOfBirthPage.nextPage(NormalMode, answers) mustEqual routes.UkAddressController.onPageLoad(NormalMode)
      }

      "must go to error page if no data" in {

        DateOfBirthPage.nextPage(NormalMode, emptyAnswers) mustEqual routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        DateOfBirthPage.nextPage(CheckMode, emptyAnswers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
