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

import builders.UserAnswersBuilder.anEmptyAnswer
import forms.RegistrationConfirmationFormProvider
import models.RegistrationType.ThirdParty
import models.subscription.requests.SubscriptionRequest
import models.subscription.responses.SubscribedResponse
import models.subscription.{Individual, IndividualContact}
import models.{IndividualName, NormalMode, RegistrationType, SubscriptionDetails, UserAnswers}
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.{RegistrationConfirmationPage, RegistrationTypePage}

trait SetupForRegistrationConfirmation extends TryValues with OptionValues {
  protected lazy val fakeAnswers: UserAnswers =
    setupAnswers("", "", None, ThirdParty)

  protected def setupAnswers(
                              dprsUserId: String,
                              primaryEmail: String,
                              secondaryEmail: Option[String],
                              registrationType: RegistrationType
                            ): UserAnswers = {
    anEmptyAnswer.copy(
      subscriptionDetails = Some(SubscriptionDetails(
        subscriptionResponse = SubscribedResponse(dprsUserId),
        subscriptionRequest = SubscriptionRequest(
          "",
          false,
          None,
          getContact(primaryEmail),
          secondaryEmail.map(e => getContact(e))
        ),
        registrationType = registrationType,
        businessType = None
      ))
    ).set(RegistrationTypePage, registrationType).success.value
  }

  private def getContact(email: String) = IndividualContact(
    Individual(IndividualName("", "")),
    email,
    None
  )
}

class RegistrationConfirmationViewModelSpec extends AnyFreeSpec with Matchers with SetupForRegistrationConfirmation {

  private val anyMode = NormalMode
  private val formProvider = new RegistrationConfirmationFormProvider()

  private val underTest = RegistrationConfirmationViewModel
  private val isPrivateBeta = false

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when RegistrationConfirmationPage answer available" in {
      val form = formProvider()
      val anyBoolean = true
      val userAnswers = fakeAnswers.set(RegistrationConfirmationPage, anyBoolean).get

      underTest.apply(anyMode, userAnswers, form, isPrivateBeta) mustBe
        Some(RegistrationConfirmationViewModel(
          mode = anyMode,
          form = form.fill(anyBoolean),
          dprsUserId = "",
          primaryEmail = "",
          secondaryEmail = None,
          isThirdParty = true,
          isPrivateBeta = isPrivateBeta
        ))
    }

    "must return ViewModel without pre-filled form when RegistrationConfirmationPage answer not available" in {
      val emptyForm = formProvider()
      val userAnswers = fakeAnswers.remove(RegistrationConfirmationPage).get

      underTest.apply(anyMode, userAnswers, emptyForm, isPrivateBeta) mustBe
        Some(RegistrationConfirmationViewModel(
          mode = anyMode,
          form = emptyForm,
          dprsUserId = "",
          primaryEmail = "",
          secondaryEmail = None,
          isThirdParty = true,
          isPrivateBeta = isPrivateBeta
        ))
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider().bind(Map(RegistrationConfirmationPage.toString -> "unknown-value"))
      val userAnswers = fakeAnswers.remove(RegistrationConfirmationPage).get

      underTest.apply(anyMode, userAnswers, formWithErrors, isPrivateBeta) mustBe
        Some(RegistrationConfirmationViewModel(
          mode = anyMode,
          form = formWithErrors,
          dprsUserId = "",
          primaryEmail = "",
          secondaryEmail = None,
          isThirdParty = true,
          isPrivateBeta = isPrivateBeta
        ))
    }

    "must return data is invalid" in {
      underTest.apply(anyMode, anEmptyAnswer, formProvider(), isPrivateBeta) mustBe None
    }
  }
}
