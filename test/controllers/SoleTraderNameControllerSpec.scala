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
import connectors.RegistrationConnector
import forms.SoleTraderNameFormProvider
import models.registration.requests.{IndividualDetails, IndividualWithUtr}
import models.registration.responses.NoMatchResponse
import models.{NormalMode, SoleTraderName, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{SoleTraderNamePage, UtrPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.SoleTraderNameView

import scala.concurrent.Future

class SoleTraderNameControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new SoleTraderNameFormProvider()
  private val form = formProvider()

  private lazy val soleTraderNameRoute = routes.SoleTraderNameController.onPageLoad(NormalMode).url

  private val userAnswers = minimalUserAnswers.set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value

  "SoleTraderName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(minimalUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, soleTraderNameRoute)

        val view = application.injector.instanceOf[SoleTraderNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, soleTraderNameRoute)

        val view = application.injector.instanceOf[SoleTraderNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(SoleTraderName("first", "last")), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page and store the registration result when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockConnector = mock[RegistrationConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockConnector.register(any())(any())) thenReturn Future.successful(NoMatchResponse())

      val baseAnswers = minimalUserAnswers.set(UtrPage, "123").success.value
      
      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[RegistrationConnector].toInstance(mockConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, soleTraderNameRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"))

        val expectedRegistrationRequest = IndividualWithUtr("123", IndividualDetails("first", "last"))
        
        val expectedAnswers =
          baseAnswers
            .set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value
            .copy(registrationResponse = Some(NoMatchResponse()))

        val answersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual SoleTraderNamePage.nextPage(NormalMode, expectedAnswers).url
        verify(mockConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
        verify(mockSessionRepository, times(1)).set(answersCaptor.capture())

        val savedAnswers = answersCaptor.getValue
        savedAnswers.registrationResponse.value mustEqual NoMatchResponse()
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(minimalUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, soleTraderNameRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SoleTraderNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, soleTraderNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, soleTraderNameRoute)
            .withFormUrlEncodedBody(("firstName", "first"), ("lastName", "last"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
