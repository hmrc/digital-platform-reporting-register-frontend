/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import base.ControllerSpecBase
import builders.MatchResponseWithIdBuilder.aMatchResponseWithId
import builders.UkAddressBuilder.aUkAddress
import builders.UserAnswersBuilder.anEmptyAnswer
import builders.UserBuilder.aUser
import models.registration.RegisteredAddressCountry
import models.registration.responses.NoMatchResponse
import models.{BusinessType, IndividualName, NormalMode, Utr}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.MissingInformationView

import java.time.LocalDate

class MissingInformationControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  "MissingInformation Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, routes.MissingInformationController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[MissingInformationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must redirect to CheckYourAnswersController when registration does not exists and RegistrationRequest data available" in {
      val userAnswers = anEmptyAnswer
        .set(BusinessTypePage, BusinessType.SoleTrader).success.value
        .set(IndividualNamePage, IndividualName("some-first-name", "some-last-name")).success.value
        .set(DateOfBirthPage, LocalDate.parse("2000-01-01")).success.value
        .set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
        .set(UkAddressPage, aUkAddress).success.value
        .set(IndividualEmailAddressPage, "some.email@example.com").success.value
        .set(CanPhoneIndividualPage, false).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.MissingInformationController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to BusinessTypeController when registration does not exists and RegistrationRequest data not available" in {
      val userAnswers = anEmptyAnswer
        .set(BusinessTypePage, BusinessType.SoleTrader).success.value
        .set(IndividualNamePage, IndividualName("some-first-name", "some-last-name")).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.MissingInformationController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.BusinessTypeController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to CheckYourAnswersController when MatchResponse registration and SubscriptionRequest data available" in {
      val userAnswers = anEmptyAnswer
        .copy(registrationResponse = Some(aMatchResponseWithId))
        .copy(user = aUser.copy(taxIdentifier = Some(Utr("any-utr"))))
        .set(PrimaryContactNamePage, "some-name").success.value
        .set(PrimaryContactEmailAddressPage, "some.email@example.com").success.value
        .set(CanPhonePrimaryContactPage, false).success.value
        .set(HasSecondaryContactPage, false).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.MissingInformationController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "must redirect to PrimaryContactNameController when MatchResponse registration and SubscriptionRequest data not available" in {
      val userAnswers = anEmptyAnswer
        .copy(registrationResponse = Some(aMatchResponseWithId))
        .copy(user = aUser.copy(taxIdentifier = Some(Utr("any-utr"))))
        .set(PrimaryContactNamePage, "some-name").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.MissingInformationController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PrimaryContactNameController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to CheckYourAnswersController when any non MatchResponse registration" in {
      val userAnswers = anEmptyAnswer
        .copy(registrationResponse = Some(NoMatchResponse()))
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.MissingInformationController.onSubmit().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url
      }
    }
  }
}
