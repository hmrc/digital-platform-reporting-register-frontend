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
import builders.AddressBuilder.anAddress
import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.registration.responses.{AlreadySubscribedResponse, MatchResponseWithId, NoMatchResponse}
import models.{BusinessType, CheckMode, NormalMode}
import org.scalatest.{OptionValues, TryValues}

class BusinessNamePageSpec extends SpecBase with TryValues with OptionValues {

  ".nextPage" - {
    val noResponseAnswer = anEmptyAnswer.copy(registrationResponse = Some(NoMatchResponse()))

    "in Normal Mode" - {
      "ETMP returns match & account already registered leads to BusinessAlreadyRegistered" in {
        val alreadySubscribedAnswer = anEmptyAnswer.copy(registrationResponse = Some(AlreadySubscribedResponse()))
        BusinessNamePage.nextPage(NormalMode, alreadySubscribedAnswer) mustEqual routes.BusinessAlreadyRegisteredController.onPageLoad()
      }

      "ETMP returns match & account not already registered leads to DetailsMatched" in {
        val responseAnswer = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithId("Id", anAddress, Some("name"))))
        BusinessNamePage.nextPage(NormalMode, responseAnswer) mustEqual routes.DetailsMatchedController.onPageLoad()
      }

      "ETMP returns no match & user Sole Trader leads to SoleTraderDetailsNotMatch" in {
        val answers = noResponseAnswer.set(BusinessTypePage, BusinessType.SoleTrader).success.value
        BusinessNamePage.nextPage(NormalMode, answers) mustEqual routes.SoleTraderDetailsNotMatchController.onPageLoad()
      }

      "ETMP returns no match & user not Sole Trader leads to BusinessDetailsDoNotMatch" in {
        val answers = noResponseAnswer.set(BusinessTypePage, BusinessType.Partnership).success.value
        BusinessNamePage.nextPage(NormalMode, answers) mustEqual routes.BusinessDetailsDoNotMatchController.onPageLoad()
      }

      "Response not matched & User has no businessType leads to JourneyRecovery" in {
        BusinessNamePage.nextPage(NormalMode, noResponseAnswer) mustEqual routes.JourneyRecoveryController.onPageLoad()
      }

      "No response leads to JourneyRecovery" in {
        BusinessNamePage.nextPage(NormalMode, anEmptyAnswer) mustEqual routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        BusinessNamePage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
