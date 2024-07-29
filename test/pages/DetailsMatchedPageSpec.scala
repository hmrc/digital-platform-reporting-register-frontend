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
import models.{BusinessType, CheckMode, NormalMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}

class DetailsMatchedPageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id", None)

    "in Normal Mode" - {
      "must go to 'set-up-contact-details' if user is Organisation" in {
        val answers = emptyAnswers.set(BusinessTypePage, BusinessType.Partnership).success.value
        DetailsMatchedPage.nextPage(NormalMode, answers) mustEqual routes.ContactDetailsGuidanceController.onPageLoad()
      }

      "must go to 'individual-email-address' if user is Sole Trader" in {
        val answers = emptyAnswers.set(BusinessTypePage, BusinessType.SoleTrader).success.value
        DetailsMatchedPage.nextPage(NormalMode, answers) mustEqual routes.IndividualEmailAddressController.onPageLoad(NormalMode)
      }

      "must go to 'individual-email-address' if user is Individual" in {
        val answers = emptyAnswers.set(BusinessTypePage, BusinessType.Individual).success.value
        DetailsMatchedPage.nextPage(NormalMode, answers) mustEqual routes.IndividualEmailAddressController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        DetailsMatchedPage.nextPage(CheckMode, emptyAnswers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
