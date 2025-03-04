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

package models.pageviews

import base.SpecBase
import builders.OrganisationContactBuilder.anOrganisationContact
import builders.SubscribedResponseBuilder.aSubscribedResponse
import builders.SubscriptionDetailsBuilder.aSubscriptionDetails
import builders.SubscriptionRequestBuilder.aSubscriptionRequest
import builders.UserAnswersBuilder.aUserAnswers
import models.{BusinessType, RegistrationType, SubscriptionDetails}
import pages.RegistrationConfirmationPage

import java.time.Instant

class RegistrationConfirmationViewModelSpec extends SpecBase {

  private val subscribedResponse = aSubscribedResponse.copy(dprsId = "some-dprs-id", Instant.parse("2024-03-17T09:30:47Z"))
  private val subscriptionRequest = aSubscriptionRequest.copy(primaryContact = anOrganisationContact.copy(email = "primary.email@example.com"))
  private val subscriptionDetails = aSubscriptionDetails.copy(
    subscriptionResponse = subscribedResponse,
    subscriptionRequest = subscriptionRequest,
    registrationType = RegistrationType.ThirdParty,
    emailSent = true
  )

  private val underTest = RegistrationConfirmationViewModel

  ".apply(...)" - {
    "must return ViewModel" in {
      val anyBoolean = true

      val userAnswers = aUserAnswers.copy(subscriptionDetails = Some(subscriptionDetails))
        .set(RegistrationConfirmationPage, anyBoolean).get

      underTest.apply(userAnswers) mustBe
        Some(RegistrationConfirmationViewModel(
          dprsId = "some-dprs-id",
          subscribedDateTime = "17 March 2024 at 9:30am (GMT)",
          primaryEmail = "primary.email@example.com",
          isThirdParty = true,
          emailSent = subscriptionDetails.emailSent
        ))
    }

    "must return None when Subscription details do not exist" in {
      val userAnswers = aUserAnswers.copy(subscriptionDetails = None)

      underTest.apply(userAnswers) mustBe None
    }
  }
}
