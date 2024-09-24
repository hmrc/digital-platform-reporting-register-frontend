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

class HasSecondaryContactPageSpec extends SpecBase with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Secondary Contact Name when the answer is yes" in {
        val answers = anEmptyAnswer.set(HasSecondaryContactPage, true).success.value
        HasSecondaryContactPage.nextPage(NormalMode, answers) mustEqual routes.SecondaryContactNameController.onPageLoad(NormalMode)
      }

      "must go to Check Answers when the answer is no" in {
        val answers = anEmptyAnswer.set(HasSecondaryContactPage, false).success.value
        HasSecondaryContactPage.nextPage(NormalMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }

    "in Check Mode" - {
      "must go to Secondary Contact Name when the answer is yes and answers do not contain a secondary contact name" in {
        val answers = anEmptyAnswer.set(HasSecondaryContactPage, true).success.value
        HasSecondaryContactPage.nextPage(CheckMode, answers) mustEqual routes.SecondaryContactNameController.onPageLoad(CheckMode)
      }

      "must go to Check Answers" - {

        "when the answer is yes and answers contains a secondary contact name" in {
          val answers =
            anEmptyAnswer
              .set(HasSecondaryContactPage, true).success.value
              .set(SecondaryContactNamePage, "name").success.value
          HasSecondaryContactPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }

        "when the answer is no" in {
          val answers = anEmptyAnswer.set(HasSecondaryContactPage, false).success.value
          HasSecondaryContactPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }
      }
    }
  }

  ".cleanup" - {

    "must remove all secondary contact data when the answer is no" in {
      val initialAnswers =
        anEmptyAnswer
          .set(SecondaryContactNamePage, "name").success.value
          .set(SecondaryContactEmailAddressPage, "email").success.value
          .set(CanPhoneSecondaryContactPage, true).success.value
          .set(SecondaryContactPhoneNumberPage, "phone").success.value

      val result = initialAnswers.set(HasSecondaryContactPage, false).success.value

      result.get(SecondaryContactNamePage) must not be defined
      result.get(SecondaryContactEmailAddressPage) must not be defined
      result.get(CanPhoneSecondaryContactPage) must not be defined
      result.get(SecondaryContactPhoneNumberPage) must not be defined
    }

    "must not remove data when the answer is yes" in {
      val initialAnswers =
        anEmptyAnswer
          .set(SecondaryContactNamePage, "name").success.value
          .set(SecondaryContactEmailAddressPage, "email").success.value
          .set(CanPhoneSecondaryContactPage, true).success.value
          .set(SecondaryContactPhoneNumberPage, "phone").success.value

      val result = initialAnswers.set(HasSecondaryContactPage, true).success.value

      result.get(SecondaryContactNamePage) mustBe defined
      result.get(SecondaryContactEmailAddressPage) mustBe defined
      result.get(CanPhoneSecondaryContactPage) mustBe defined
      result.get(SecondaryContactPhoneNumberPage) mustBe defined
    }
  }
}
