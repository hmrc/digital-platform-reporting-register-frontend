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

class SecondaryContactNamePageSpec extends SpecBase with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Secondary Contact Email Address" in {
        SecondaryContactNamePage.nextPage(NormalMode, anEmptyAnswer) mustEqual routes.SecondaryContactEmailAddressController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to Check Answers when Secondary Contact Email Address has been answered" in {
        val answers = anEmptyAnswer.set(SecondaryContactEmailAddressPage, "email").success.value
        SecondaryContactNamePage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }

      "must go to Secondary Contact Email Address when it has not been answered" in {
        SecondaryContactNamePage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.SecondaryContactEmailAddressController.onPageLoad(CheckMode)
      }
    }
  }
}
