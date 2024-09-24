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

class SecondaryContactEmailAddressPageSpec extends SpecBase with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Can Phone Secondary Contact" in {
        SecondaryContactEmailAddressPage.nextPage(NormalMode, anEmptyAnswer) mustEqual routes.CanPhoneSecondaryContactController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to Check Answers when Can Phone Secondary Contact has been answered" in {
        val anyBoolean = true
        val answers = anEmptyAnswer.set(CanPhoneSecondaryContactPage, anyBoolean).success.value
        SecondaryContactEmailAddressPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to Can Phone Secondary Contact when it has not been answered" in {
        SecondaryContactEmailAddressPage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CanPhoneSecondaryContactController.onPageLoad(CheckMode)
      }
    }
  }
}
