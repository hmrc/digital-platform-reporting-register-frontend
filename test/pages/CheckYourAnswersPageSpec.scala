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
import builders.SubscriptionDetailsBuilder
import builders.SubscriptionDetailsBuilder.aSubscriptionDetails
import builders.SubscriptionRequestBuilder.aSubscriptionRequest
import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.BusinessType.*
import models.registration.responses as registrationResponses
import models.subscription.requests.SubscriptionRequest
import models.subscription.responses as subscriptionResponses
import models.{BusinessType, NormalMode, RegistrationType, SubscriptionDetails}
import org.scalacheck.Gen
import org.scalatest.{OptionValues, TryValues}

import java.time.Instant

class CheckYourAnswersPageSpec extends SpecBase with TryValues with OptionValues {

  ".nextPage" - {
    "must go to journey recovery when there is no registration response" in {
      CheckYourAnswersPage.nextPage(NormalMode, anEmptyAnswer) mustEqual routes.JourneyRecoveryController.onPageLoad()
    }

    "must go to journey recovery when the registration response is No Match" in {
      val answers = anEmptyAnswer.copy(registrationResponse = Some(registrationResponses.NoMatchResponse()))
      CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.JourneyRecoveryController.onPageLoad()
    }

    "must go to Individual Already Registered when the registration response is AlreadySubscribed and the user is an individual type" in {
      val businessType = Gen.oneOf(Individual, SoleTrader).sample.value
      val answers = anEmptyAnswer.copy(
        registrationResponse = Some(registrationResponses.AlreadySubscribedResponse()),
        subscriptionDetails = Some(aSubscriptionDetails.copy(businessType = Some(businessType)))
      )

      CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.IndividualAlreadyRegisteredController.onPageLoad()
    }

    "must go to Organisation Already Registered when the registration response is AlreadySubscribed and the user is an organisation type" in {
      val businessType = Gen.oneOf(LimitedCompany, Llp, Partnership, AssociationOrTrust).sample.value
      val answers = anEmptyAnswer.copy(
        registrationResponse = Some(registrationResponses.AlreadySubscribedResponse()),
        subscriptionDetails = Some(aSubscriptionDetails.copy(businessType = Some(businessType)))
      )

      CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.BusinessAlreadyRegisteredController.onPageLoad()
    }

    "must go to error page when no subscription details" in {
      val answers = anEmptyAnswer.copy(registrationResponse = Some(registrationResponses.AlreadySubscribedResponse()))

      CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.JourneyRecoveryController.onPageLoad()
    }

    "when the registration was successful" - {
      val aRegistrationResponse = registrationResponses.MatchResponseWithoutId("safeId")
      val baseAnswers = anEmptyAnswer.copy(registrationResponse = Some(aRegistrationResponse))

      "must go to registration confirmation when the subscription was successful" in {
        val subscriptionResponse = subscriptionResponses.SubscribedResponse("dprsId", Instant.now())
        val subscriptionDetails = SubscriptionDetails(subscriptionResponse, aSubscriptionRequest, RegistrationType.PlatformOperator, None, Some(""), false)
        val answers = baseAnswers.copy(subscriptionDetails = Some(subscriptionDetails))

        CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.RegistrationConfirmationController.onPageLoad(NormalMode)
      }

      "must go to Individual Already Registered when the subscription response is AlreadySubscribed and the user is an individual type" in {
        val subscriptionResponse = subscriptionResponses.AlreadySubscribedResponse()
        val businessType = Gen.oneOf(Individual, SoleTrader).sample.value
        val subscriptionDetails = SubscriptionDetails(subscriptionResponse, aSubscriptionRequest, RegistrationType.PlatformOperator, Some(businessType), None, false)
        val answers = baseAnswers.copy(subscriptionDetails = Some(subscriptionDetails))

        CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.IndividualAlreadyRegisteredController.onPageLoad()
      }

      "must go to Organisation Already Registered when the subscription response is AlreadySubscribed and the user is an organisation type" in {
        val subscriptionResponse = subscriptionResponses.AlreadySubscribedResponse()
        val businessType = Gen.oneOf(LimitedCompany, Llp, Partnership, AssociationOrTrust).sample.value
        val subscriptionDetails = SubscriptionDetails(subscriptionResponse, aSubscriptionRequest, RegistrationType.PlatformOperator, Some(businessType), None, false)
        val answers = baseAnswers.copy(subscriptionDetails = Some(subscriptionDetails))

        CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.BusinessAlreadyRegisteredController.onPageLoad()
      }

      "must go to Business Already Registered when the subscription response is AlreadySubscribed and business type is not answered" in {
        val subscriptionResponse = subscriptionResponses.AlreadySubscribedResponse()
        val subscriptionDetails = SubscriptionDetails(subscriptionResponse, aSubscriptionRequest, RegistrationType.PlatformOperator, None, None, false)
        val answers = baseAnswers.copy(subscriptionDetails = Some(subscriptionDetails))

        CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.BusinessAlreadyRegisteredController.onPageLoad()
      }
    }
  }
}
