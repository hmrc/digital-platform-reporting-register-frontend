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

package controllers

import base.SpecBase
import builders.UserAnswersBuilder.{aUserAnswers, anEmptyAnswer}
import config.FrontendAppConfig
import forms.RegistrationConfirmationFormProvider
import generators.{Generators, ModelGenerators}
import models.pageviews.RegistrationConfirmationViewModel
import models.{NormalMode, RegistrationType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.RegistrationConfirmationPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.RegistrationConfirmationView

import scala.concurrent.Future

class RegistrationConfirmationControllerSpec extends SpecBase with MockitoSugar with Generators with ModelGenerators {

  private val form = new RegistrationConfirmationFormProvider()()
  private lazy val registrationConfirmationRoute = routes.RegistrationConfirmationController.onPageLoad(NormalMode).url

  "RegistrationConfirmation Controller" - {
    "must return OK and the correct view for a GET" in forAll(
      Gen.alphaStr.suchThat(_.nonEmpty),
      Gen.alphaStr.suchThat(_.nonEmpty),
      Arbitrary.arbitrary[Option[String]],
      Arbitrary.arbitrary[RegistrationType]
    ) {
      (
        dprsUserId: String,
        primaryEmail: String,
        secondaryEmail: Option[String],
        registrationType: RegistrationType
      ) =>
        val application = applicationBuilder(userAnswers = Some(aUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, registrationConfirmationRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[RegistrationConfirmationView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val viewModel = RegistrationConfirmationViewModel(NormalMode, aUserAnswers, form, appConfig.isPrivateBeta)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel.get)(request, messages(application)).toString
        }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in forAll(
      Gen.alphaStr.suchThat(_.nonEmpty),
      Gen.alphaStr.suchThat(_.nonEmpty),
      Arbitrary.arbitrary[Option[String]],
      Arbitrary.arbitrary[RegistrationType]
    ) {
      (
        dprsUserId: String,
        primaryEmail: String,
        secondaryEmail: Option[String],
        registrationType: RegistrationType
      ) =>
        val userAnswers = aUserAnswers.set(RegistrationConfirmationPage, true).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, registrationConfirmationRoute)
          val view = application.injector.instanceOf[RegistrationConfirmationView]
          val result = route(application, request).value
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val viewModel = RegistrationConfirmationViewModel(NormalMode, userAnswers, form.fill(true), appConfig.isPrivateBeta)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel.get)(request, messages(application)).toString
        }
    }

    "must go to error page if invalid data" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, registrationConfirmationRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(aUserAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(POST, registrationConfirmationRoute)
          .withFormUrlEncodedBody(("value", "true"))
        val updatedAnswers = aUserAnswers.set(RegistrationConfirmationPage, true).success.value
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RegistrationConfirmationPage.nextPage(NormalMode, updatedAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(aUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, registrationConfirmationRoute)
          .withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[RegistrationConfirmationView]
        val result = route(application, request).value
        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val viewModel = RegistrationConfirmationViewModel(NormalMode, aUserAnswers, boundForm, appConfig.isPrivateBeta)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(viewModel.get)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, registrationConfirmationRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, registrationConfirmationRoute)
          .withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
