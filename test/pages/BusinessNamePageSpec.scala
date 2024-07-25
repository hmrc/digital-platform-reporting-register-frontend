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
import models.registration.Address
import models.registration.responses.{MatchResponseWithId, NoMatchResponse}
import models.{BusinessType, CheckMode, NormalMode, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}

class BusinessNamePageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".nextPage" - {
    val emptyAnswers = UserAnswers("id", None)
    val address = Address("line 1", None, None, None, None, "GB")
    val responseAnswer = emptyAnswers.copy(registrationResponse = Some(MatchResponseWithId("Id", address, Some("name"))))
    val noResponseAnswer = emptyAnswers.copy(registrationResponse = Some(NoMatchResponse()))

    "in Normal Mode" - {

      //TODO: update test when navigation sorted
      "ETMP returns match & account already registered" in {
        val answers = responseAnswer.set(BusinessTypePage, BusinessType.Partnership).success.value
        
        BusinessNamePage.nextPage(NormalMode, answers) mustEqual routes.IndexController.onPageLoad()
      }
      //TODO: update test when navigation sorted
      "ETMP returns match & account not already registered" in {
        BusinessNamePage.nextPage(NormalMode, responseAnswer) mustEqual routes.IndexController.onPageLoad()
      }
      "ETMP returns no match & user Sole Trader" in {
        val answers = noResponseAnswer.set(BusinessTypePage, BusinessType.SoleTrader).success.value

        BusinessNamePage.nextPage(NormalMode, answers) mustEqual routes.SoleTraderDetailsNotMatchController.onPageLoad()
      }
      //TODO: update test when page created
      "ETMP returns no match & user not Sole Trader" in {
        val answers = noResponseAnswer.set(BusinessTypePage, BusinessType.Partnership).success.value

        BusinessNamePage.nextPage(NormalMode, answers) mustEqual routes.IndexController.onPageLoad()
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        BusinessNamePage.nextPage(CheckMode, emptyAnswers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
