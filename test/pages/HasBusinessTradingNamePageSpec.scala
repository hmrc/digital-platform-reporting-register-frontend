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
import builders.UserAnswersBuilder.{aUserAnswers, anEmptyAnswer}
import controllers.routes
import models.{CheckMode, NormalMode}
import org.scalatest.TryValues

class HasBusinessTradingNamePageSpec extends SpecBase with TryValues {

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
      "must go to Check Answers" - {
        "when has trading name is true and trading name provided" in {
          val userAnswers = aUserAnswers
            .set(HasBusinessTradingNamePage, true).success.value
            .set(BusinessEnterTradingNamePage, "any-business-trading-name").success.value
          HasBusinessTradingNamePage.nextPage(CheckMode, userAnswers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }

        "when has trading name is false" in {
          val userAnswers = aUserAnswers.set(HasBusinessTradingNamePage, false).success.value
          HasBusinessTradingNamePage.nextPage(CheckMode, userAnswers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }
      }

      "must go to Enter Trading Name when has trading name is true and answers has no trading name" in {
        val userAnswers = aUserAnswers
          .set(HasBusinessTradingNamePage, true).success.value
          .remove(BusinessEnterTradingNamePage).success.value
        HasBusinessTradingNamePage.nextPage(CheckMode, userAnswers) mustEqual routes.BusinessEnterTradingNameController.onPageLoad(CheckMode)
      }

      "must go to journey recovery when answers does not have a value for has trading name question" in {
        val userAnswers = aUserAnswers.remove(HasBusinessTradingNamePage).success.value
        HasBusinessTradingNamePage.nextPage(CheckMode, userAnswers) mustEqual routes.JourneyRecoveryController.onPageLoad()
      }
    }
  }

  ".cleanup" - {
    "must remove Business trading name when the answer is No" in {
      val initialAnswers = aUserAnswers.set(BusinessEnterTradingNamePage, "any-business-trading-name").success.value
      val result = initialAnswers.set(HasBusinessTradingNamePage, false).success.value
      result.get(BusinessEnterTradingNamePage) must not be defined
    }

    "must not remove Business trading name when answer is Yes" in {
      val initialAnswers = aUserAnswers.set(BusinessEnterTradingNamePage, "any-business-trading-name").success.value
      val result = initialAnswers.set(HasBusinessTradingNamePage, true).success.value
      result.get(BusinessEnterTradingNamePage) mustBe Some("any-business-trading-name")
    }
  }
}
