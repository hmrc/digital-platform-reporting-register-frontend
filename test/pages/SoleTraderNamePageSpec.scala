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

import builders.AddressBuilder.anAddress
import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.registration.responses.{AlreadySubscribedResponse, MatchResponseWithId, NoMatchResponse}
import models.{BusinessType, CheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}

class SoleTraderNamePageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".nextPage" - {
    val noResponseAnswer = anEmptyAnswer.copy(registrationResponse = Some(NoMatchResponse()))

    "in Normal Mode" - {
      "ETMP returns match & account already registered leads to 'business-already-registered' page" in {
        val alreadySubscribedAnswer = anEmptyAnswer.copy(registrationResponse = Some(AlreadySubscribedResponse()))
        SoleTraderNamePage.nextPage(NormalMode, alreadySubscribedAnswer) mustEqual routes.IndividualAlreadyRegisteredController.onPageLoad()
      }

      "ETMP returns match & account not already registered leads to 'we have matched your details' page" in {
        val responseAnswer = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithId("Id", anAddress, Some("name"))))
        SoleTraderNamePage.nextPage(NormalMode, responseAnswer) mustEqual routes.DetailsMatchedController.onPageLoad()
      }

      "ETMP returns no match & user Sole Trader leads to 'sole-trader-details-not-match' page" in {
        val answers = noResponseAnswer.set(BusinessTypePage, BusinessType.SoleTrader).success.value
        SoleTraderNamePage.nextPage(NormalMode, answers) mustEqual routes.SoleTraderDetailsNotMatchController.onPageLoad()
      }

      "ETMP returns no match & user not Sole Trader leads to 'business-details-not-match' page" in {
        val answers = noResponseAnswer.set(BusinessTypePage, BusinessType.Partnership).success.value
        SoleTraderNamePage.nextPage(NormalMode, answers) mustEqual routes.BusinessDetailsDoNotMatchController.onPageLoad()
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        SoleTraderNamePage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
