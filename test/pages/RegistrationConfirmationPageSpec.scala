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
import models.{CheckMode, NormalMode}
import org.scalatest.TryValues

class RegistrationConfirmationPageSpec extends SpecBase with TryValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Index if answer is true" in {
        val answers = anEmptyAnswer.set(RegistrationConfirmationPage, true).success.value
        RegistrationConfirmationPage.nextPage(NormalMode, answers) mustEqual routes.IndexController.onPageLoad()
      }
      "must go to Index if answer is false" in {
        val answers = anEmptyAnswer.set(RegistrationConfirmationPage, false).success.value
        RegistrationConfirmationPage.nextPage(NormalMode, answers) mustEqual routes.IndexController.onPageLoad()
      }
      "must go to error page if answer absent" in {
        RegistrationConfirmationPage.nextPage(NormalMode, anEmptyAnswer) mustEqual routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        RegistrationConfirmationPage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
