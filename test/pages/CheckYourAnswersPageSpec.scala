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
import models.BusinessType.*
import models.registration.responses as registrationResponses
import models.subscription.requests.SubscriptionRequest
import models.subscription.{IndividualContact, responses as subscriptionResponses}
import models.{NormalMode, RegistrationType, SubscriptionDetails, UserAnswers}
import org.scalacheck.Gen
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class CheckYourAnswersPageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id", None)
    val subscriptionRequest = SubscriptionRequest(
      "safeId",
      true,
      None,
      IndividualContact(models.subscription.Individual("first", "last"), "email", None),
      None
    )

    "must go to journey recovery when there is no registration response" in {

      CheckYourAnswersPage.nextPage(NormalMode, emptyAnswers) mustEqual routes.JourneyRecoveryController.onPageLoad()
    }

    "must go to journey recovery when the registration response is No Match" in {

      val answers = emptyAnswers.copy(registrationResponse = Some(registrationResponses.NoMatchResponse()))
      CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.JourneyRecoveryController.onPageLoad()
    }

    "must go to Individual Already Registered when the registration response is AlreadySubscribed and the user is an individual type" in {

      val businessType = Gen.oneOf(Individual, SoleTrader).sample.value
      val answers =
        emptyAnswers
          .copy(registrationResponse = Some(registrationResponses.AlreadySubscribedResponse()))
          .set(BusinessTypePage, businessType).success.value

      CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.IndividualAlreadyRegisteredController.onPageLoad()
    }

    "must go to Organisation Already Registered when the registration response is AlreadySubscribed and the user is an organisation type" in {

      val businessType = Gen.oneOf(LimitedCompany, Llp, Partnership, AssociationOrTrust).sample.value
      val answers =
        emptyAnswers
          .copy(registrationResponse = Some(registrationResponses.AlreadySubscribedResponse()))
          .set(BusinessTypePage, businessType).success.value

      CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.BusinessAlreadyRegisteredController.onPageLoad()
    }

    "must go to Organisation Already Registered when the registration response is AlreadySubscribed and business type is not answered" in {

      val answers = emptyAnswers.copy(registrationResponse = Some(registrationResponses.AlreadySubscribedResponse()))

      CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.BusinessAlreadyRegisteredController.onPageLoad()
    }

    "when the registration was successful" - {

      val aRegistrationResponse = registrationResponses.MatchResponseWithoutId("safeId")
      val baseAnswers = emptyAnswers.copy(registrationResponse = Some(aRegistrationResponse))

      // TODO: Change to success page when it is available
      "must go to Index when the subscription was successful" in {

        val subscriptionResponse = subscriptionResponses.SubscribedResponse("dprsId")
        val subscriptionDetails = SubscriptionDetails(subscriptionResponse, subscriptionRequest, RegistrationType.PlatformOperator)
        val answers = baseAnswers.copy(subscriptionDetails = Some(subscriptionDetails))

        CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.IndexController.onPageLoad()
      }

      "must go to Individual Already Registered when the subscription response is AlreadySubscribed and the user is an individual type" in {

        val subscriptionResponse = subscriptionResponses.AlreadySubscribedResponse()
        val subscriptionDetails = SubscriptionDetails(subscriptionResponse, subscriptionRequest, RegistrationType.PlatformOperator)
        val businessType = Gen.oneOf(Individual, SoleTrader).sample.value
        val answers =
          baseAnswers
            .copy(subscriptionDetails = Some(subscriptionDetails))
            .set(BusinessTypePage, businessType).success.value

        CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.IndividualAlreadyRegisteredController.onPageLoad()
      }

      "must go to Organisation Already Registered when the subscription response is AlreadySubscribed and the user is an organisation type" in {

        val subscriptionResponse = subscriptionResponses.AlreadySubscribedResponse()
        val subscriptionDetails = SubscriptionDetails(subscriptionResponse, subscriptionRequest, RegistrationType.PlatformOperator)
        val businessType = Gen.oneOf(LimitedCompany, Llp, Partnership, AssociationOrTrust).sample.value
        val answers =
          baseAnswers
            .copy(subscriptionDetails = Some(subscriptionDetails))
            .set(BusinessTypePage, businessType).success.value

        CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.BusinessAlreadyRegisteredController.onPageLoad()
      }

      "must go to Organisation Already Registered when the subscription response is AlreadySubscribed and business type is not answered" in {

        val subscriptionResponse = subscriptionResponses.AlreadySubscribedResponse()
        val subscriptionDetails = SubscriptionDetails(subscriptionResponse, subscriptionRequest, RegistrationType.PlatformOperator)
        val answers = baseAnswers.copy(subscriptionDetails = Some(subscriptionDetails))

        CheckYourAnswersPage.nextPage(NormalMode, answers) mustEqual routes.BusinessAlreadyRegisteredController.onPageLoad()
      }
    }
  }
}
