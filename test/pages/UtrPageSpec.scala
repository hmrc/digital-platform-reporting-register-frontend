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
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class UtrPageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id")

    "in Normal Mode" - {

      "must go to Sole Trader Name for a Sole Trader" in {

        val answers = emptyAnswers.set(BusinessTypePage, BusinessType.SoleTrader).success.value
        UtrPage.nextPage(NormalMode, answers) mustEqual routes.SoleTraderNameController.onPageLoad(NormalMode)
      }

      "must go to Business Name for other business types" in {

        for (businessType <- BusinessType.values.filterNot(_ == BusinessType.SoleTrader)) {
          
          val answers = emptyAnswers.set(BusinessTypePage, businessType).success.value
          UtrPage.nextPage(NormalMode, answers) mustEqual routes.BusinessNameController.onPageLoad(NormalMode)
        }
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        UtrPage.nextPage(CheckMode, emptyAnswers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
