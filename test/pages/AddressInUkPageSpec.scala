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

import builders.InternationalAddressBuilder.anInternationalAddress
import builders.UkAddressBuilder.aUkAddress
import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.{CheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}

class AddressInUkPageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Uk Address page if yes" in {
        val answers = anEmptyAnswer.set(AddressInUkPage, true).success.value
        AddressInUkPage.nextPage(NormalMode, answers) mustEqual routes.UkAddressController.onPageLoad(NormalMode)
      }

      "must go to International Address page if no" in {
        val answers = anEmptyAnswer.set(AddressInUkPage, false).success.value
        AddressInUkPage.nextPage(NormalMode, answers) mustEqual routes.InternationalAddressController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to UK address when the answer is yes and answers do not contain a UK address" in {
        val answers = anEmptyAnswer.set(AddressInUkPage, true).success.value
        AddressInUkPage.nextPage(CheckMode, answers) mustEqual routes.UkAddressController.onPageLoad(CheckMode)
      }

      "must go to International address when the answer is no and answers do not contain an International address" in {
        val answers = anEmptyAnswer.set(AddressInUkPage, false).success.value
        AddressInUkPage.nextPage(CheckMode, answers) mustEqual routes.InternationalAddressController.onPageLoad(CheckMode)
      }

      "must go to Check Answers" - {
        "when the answer is yes and answers contains a UK address" in {
          val answers = anEmptyAnswer
            .set(AddressInUkPage, true).success.value
            .set(UkAddressPage, aUkAddress).success.value
          AddressInUkPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }

        "when the answer is no and answers contains an international address" in {
          val answers = anEmptyAnswer
            .set(AddressInUkPage, false).success.value
            .set(InternationalAddressPage, anInternationalAddress).success.value
          AddressInUkPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }
      }
    }
  }

  ".cleanup" - {
    "must remove International address when the answer is yes" in {
      val initialAnswers = anEmptyAnswer.set(InternationalAddressPage, anInternationalAddress).success.value
      val result = initialAnswers.set(AddressInUkPage, true).success.value
      result.get(InternationalAddressPage) must not be defined
    }

    "must not remove UK Address when answer is no" in {
      val initialAnswers = anEmptyAnswer.set(UkAddressPage, aUkAddress).success.value
      val result = initialAnswers.set(AddressInUkPage, false).success.value
      result.get(UkAddressPage) must not be defined
    }
  }
}
