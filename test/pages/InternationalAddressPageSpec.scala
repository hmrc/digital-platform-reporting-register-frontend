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
import builders.CountryBuilder.aCountry
import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.{CheckMode, InternationalAddress, NormalMode}
import org.scalatest.{OptionValues, TryValues}

class InternationalAddressPageSpec extends SpecBase with TryValues with OptionValues {

  private val address = InternationalAddress("line 1", None, "city", None, "Postcode", aCountry)

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Index" in {
        val answers = anEmptyAnswer.set(InternationalAddressPage, address).success.value
        InternationalAddressPage.nextPage(NormalMode, answers) mustEqual routes.IndividualEmailAddressController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        InternationalAddressPage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
