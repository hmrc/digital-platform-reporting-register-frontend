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

class CanPhoneSecondaryContactPageSpec extends SpecBase with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Secondary Contact Phone Number when the answer is yes" in {
        val answers = anEmptyAnswer.set(CanPhoneSecondaryContactPage, true).success.value
        CanPhoneSecondaryContactPage.nextPage(NormalMode, answers) mustEqual routes.SecondaryContactPhoneNumberController.onPageLoad(NormalMode)
      }

      "must go to Check Answers when the answer is no" in {
        val answers = anEmptyAnswer.set(CanPhoneSecondaryContactPage, false).success.value
        CanPhoneSecondaryContactPage.nextPage(NormalMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }

    "in Check Mode" - {
      "must go to Secondary Contact Phone Number when the answer is yes and answers do not contain a phone number" in {
        val answers = anEmptyAnswer.set(CanPhoneSecondaryContactPage, true).success.value
        CanPhoneSecondaryContactPage.nextPage(CheckMode, answers) mustEqual routes.SecondaryContactPhoneNumberController.onPageLoad(CheckMode)
      }

      "must go to Check Answers" - {

        "when the answer is yes and answers contains a phone number" in {
          val answers =
            anEmptyAnswer
              .set(CanPhoneSecondaryContactPage, true).success.value
              .set(SecondaryContactPhoneNumberPage, "phone").success.value
          CanPhoneSecondaryContactPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }

        "when the answer is no" in {
          val answers = anEmptyAnswer.set(CanPhoneSecondaryContactPage, false).success.value
          CanPhoneSecondaryContactPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }
      }
    }
  }

  ".cleanup" - {

    "must remove Secondary Contact Phone Number when the answer is no" in {
      val initialAnswers = anEmptyAnswer.set(SecondaryContactPhoneNumberPage, "phone").success.value
      val result = initialAnswers.set(CanPhoneSecondaryContactPage, false).success.value
      result.get(SecondaryContactPhoneNumberPage) must not be defined
    }

    "must not remove data when the answer is yes" in {
      val initialAnswers = anEmptyAnswer.set(SecondaryContactPhoneNumberPage, "phone").success.value
      val result = initialAnswers.set(CanPhoneSecondaryContactPage, true).success.value
      result.get(SecondaryContactPhoneNumberPage) mustBe defined
    }
  }
}
