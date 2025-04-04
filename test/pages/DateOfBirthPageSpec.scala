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
import builders.MatchResponseWithIdBuilder.aMatchResponseWithId
import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.registration.responses.{AlreadySubscribedResponse, MatchResponseWithoutId, NoMatchResponse}
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalatest.TryValues

class DateOfBirthPageSpec extends SpecBase with TryValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to `Individual already registered` if the user is already registered" in {
        val answers = anEmptyAnswer.copy(registrationResponse = Some(AlreadySubscribedResponse()))

        DateOfBirthPage.nextPage(NormalMode, answers) mustEqual routes.IndividualAlreadyRegisteredController.onPageLoad()
      }

      "must go to individual-identity-confirmed page if the user is not already registered" in {
        val answers = anEmptyAnswer.copy(registrationResponse = Some(aMatchResponseWithId))

        DateOfBirthPage.nextPage(NormalMode, answers) mustEqual routes.IndividualIdentityConfirmedController.onPageLoad()
      }

      "must go to individual-identity-not-confirmed page if a match was not found" in {
        val answers = anEmptyAnswer.copy(registrationResponse = Some(NoMatchResponse()))

        DateOfBirthPage.nextPage(NormalMode, answers) mustEqual routes.IndividualIdentityNotConfirmedController.onPageLoad()
      }

      "must go to error page if registration gave an invalid response" in {
        val answers = anEmptyAnswer.set(HasNinoPage, true).success.value
          .copy(registrationResponse = Some(MatchResponseWithoutId("")))

        DateOfBirthPage.nextPage(NormalMode, answers) mustEqual routes.JourneyRecoveryController.onPageLoad()
      }
      
      "must go to live-in-uk page if we have not tried to register the user" in {
        val answers = anEmptyAnswer

        DateOfBirthPage.nextPage(NormalMode, answers) mustEqual routes.AddressInUkController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        DateOfBirthPage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
