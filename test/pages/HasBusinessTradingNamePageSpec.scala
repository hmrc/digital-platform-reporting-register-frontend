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
import org.scalatest.TryValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class HasBusinessTradingNamePageSpec extends AnyFreeSpec with Matchers with TryValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Enter Business Trading name when answer is Yes" in {
        val answers = anEmptyAnswer.set(HasBusinessTradingNamePage, true).success.value
        HasBusinessTradingNamePage.nextPage(NormalMode, answers) mustEqual routes.BusinessEnterTradingNameController.onPageLoad(NormalMode)
      }

      "must go to What is the main address of your business when answer is No" in {
        val answers = anEmptyAnswer.set(HasBusinessTradingNamePage, false).success.value
        HasBusinessTradingNamePage.nextPage(NormalMode, answers) mustEqual routes.BusinessAddressController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        HasBusinessTradingNamePage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
