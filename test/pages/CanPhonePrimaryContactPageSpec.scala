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
import org.scalatest.{OptionValues, TryValues}

class CanPhonePrimaryContactPageSpec extends SpecBase with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Primary Contact Phone Number when the answer is yes" in {
        val answers = anEmptyAnswer.set(CanPhonePrimaryContactPage, true).success.value
        CanPhonePrimaryContactPage.nextPage(NormalMode, answers) mustEqual routes.PrimaryContactPhoneNumberController.onPageLoad(NormalMode)
      }

      "must go to Has Secondary Contact when the answer is no" in {
        val answers = anEmptyAnswer.set(CanPhonePrimaryContactPage, false).success.value
        CanPhonePrimaryContactPage.nextPage(NormalMode, answers) mustEqual routes.HasSecondaryContactController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to Primary Contact Phone Number when the answer is yes and answers do not contain a phone number" in {
        val answers = anEmptyAnswer.set(CanPhonePrimaryContactPage, true).success.value
        CanPhonePrimaryContactPage.nextPage(CheckMode, answers) mustEqual routes.PrimaryContactPhoneNumberController.onPageLoad(CheckMode)
      }

      "must go to Check Answers" - {

        "when the answer is yes and answers contains a phone number" in {
          val answers =
            anEmptyAnswer
              .set(CanPhonePrimaryContactPage, true).success.value
              .set(PrimaryContactPhoneNumberPage, "phone").success.value
          CanPhonePrimaryContactPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }

        "when the answer is no" in {
          val answers = anEmptyAnswer.set(CanPhonePrimaryContactPage, false).success.value
          CanPhonePrimaryContactPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }
      }
    }
  }

  ".cleanup" - {

    "must remove Primary Contact Phone Number when the answer is no" in {
      val initialAnswers = anEmptyAnswer.set(PrimaryContactPhoneNumberPage, "phone").success.value
      val result = initialAnswers.set(CanPhonePrimaryContactPage, false).success.value
      result.get(PrimaryContactPhoneNumberPage) must not be defined
    }

    "must not remove data when the answer is yes" in {
      val initialAnswers = anEmptyAnswer.set(PrimaryContactPhoneNumberPage, "phone").success.value
      val result = initialAnswers.set(CanPhonePrimaryContactPage, true).success.value
      result.get(PrimaryContactPhoneNumberPage) mustBe defined
    }
  }
}
