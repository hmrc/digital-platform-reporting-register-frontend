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
import models.registration.responses.{MatchResponseWithId, NoMatchResponse}
import models.{CheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}

class RegistrationTypePageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "when the user has been matched" - {
        "must go to Is This Your Business" in {
          val registrationResponse = MatchResponseWithId("safeId", anAddress, Some("name"))
          val answers = anEmptyAnswer.copy(registrationResponse = Some(registrationResponse))

          RegistrationTypePage.nextPage(NormalMode, answers) mustEqual routes.IsThisYourBusinessController.onPageLoad(NormalMode)
        }
      }

      "when the user was not matched" - {
        "must go to Business Type" in {
          val answers = anEmptyAnswer.copy(registrationResponse = Some(NoMatchResponse()))
          RegistrationTypePage.nextPage(NormalMode, answers) mustEqual routes.BusinessTypeController.onPageLoad(NormalMode)
        }
      }
    }

    "when matching was not attempted" - {
      "must go to Business Type" in {
        RegistrationTypePage.nextPage(NormalMode, anEmptyAnswer) mustEqual routes.BusinessTypeController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        RegistrationTypePage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
