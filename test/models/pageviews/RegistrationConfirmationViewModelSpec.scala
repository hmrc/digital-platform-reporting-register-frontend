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

import builders.OrganisationContactBuilder.anOrganisationContact
import builders.SubscribedResponseBuilder.aSubscribedResponse
import builders.SubscriptionDetailsBuilder.aSubscriptionDetails
import builders.SubscriptionRequestBuilder.aSubscriptionRequest
import builders.UserAnswersBuilder.{aUserAnswers, anEmptyAnswer}
import forms.RegistrationConfirmationFormProvider
import models.RegistrationType.ThirdParty
import models.{BusinessType, NormalMode, RegistrationType, SubscriptionDetails}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.RegistrationConfirmationPage

import java.time.Instant

class RegistrationConfirmationViewModelSpec extends AnyFreeSpec with Matchers {

  private val anyMode = NormalMode
  private val formProvider = new RegistrationConfirmationFormProvider()
  private val isPrivateBeta = false
  private val subscribedResponse = aSubscribedResponse.copy(dprsId = "some-dprs-id", Instant.parse("2024-03-17T09:30:47Z"))
  private val subscriptionRequest = aSubscriptionRequest.copy(primaryContact = anOrganisationContact.copy(email = "primary.email@example.com"))
  private val subscriptionDetails = aSubscriptionDetails.copy(
    subscriptionResponse = subscribedResponse,
    subscriptionRequest = subscriptionRequest,
    registrationType = RegistrationType.ThirdParty,
    businessName = Some("some-business-name")
  )

  private val underTest = RegistrationConfirmationViewModel

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when RegistrationConfirmationPage answer available" in {
      val form = formProvider()
      val anyBoolean = true

      val userAnswers = aUserAnswers.copy(subscriptionDetails = Some(subscriptionDetails))
        .set(RegistrationConfirmationPage, anyBoolean).get

      underTest.apply(anyMode, userAnswers, form, isPrivateBeta) mustBe
        Some(RegistrationConfirmationViewModel(
          mode = anyMode,
          form = form.fill(anyBoolean),
          dprsUserId = "some-dprs-id",
          subscribedDateTime = "17 March 2024 at 9:30am (GMT)",
          primaryEmail = "primary.email@example.com",
          secondaryEmail = None,
          isThirdParty = true,
          isPrivateBeta = isPrivateBeta,
          businessName = Some("some-business-name")
        ))
    }

    "must return ViewModel without pre-filled form when RegistrationConfirmationPage answer not available" in {
      val form = formProvider()
      val userAnswers = aUserAnswers.copy(subscriptionDetails = Some(subscriptionDetails))
        .remove(RegistrationConfirmationPage).get

      underTest.apply(anyMode, userAnswers, form, isPrivateBeta) mustBe
        Some(RegistrationConfirmationViewModel(
          mode = anyMode,
          form = form,
          dprsUserId = "some-dprs-id",
          subscribedDateTime = "17 March 2024 at 9:30am (GMT)",
          primaryEmail = "primary.email@example.com",
          secondaryEmail = None,
          isThirdParty = true,
          isPrivateBeta = isPrivateBeta,
          businessName = Some("some-business-name")
        ))
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider().bind(Map(RegistrationConfirmationPage.toString -> "unknown-value"))
      val userAnswers = aUserAnswers.copy(subscriptionDetails = Some(subscriptionDetails))
        .remove(RegistrationConfirmationPage).get

      underTest.apply(anyMode, userAnswers, formWithErrors, isPrivateBeta) mustBe
        Some(RegistrationConfirmationViewModel(
          mode = anyMode,
          form = formWithErrors,
          dprsUserId = "some-dprs-id",
          subscribedDateTime = "17 March 2024 at 9:30am (GMT)",
          primaryEmail = "primary.email@example.com",
          secondaryEmail = None,
          isThirdParty = true,
          isPrivateBeta = isPrivateBeta,
          businessName = Some("some-business-name")
        ))
    }

    "must return data is invalid" in {
      underTest.apply(anyMode, anEmptyAnswer, formProvider(), isPrivateBeta) mustBe None
    }
  }
}
