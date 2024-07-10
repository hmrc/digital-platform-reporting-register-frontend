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
import controllers.actions.FakeTaxIdentifierProvider
import forms.RegistrationTypeFormProvider
import models.registration.requests.OrganisationWithUtr
import models.registration.responses.NoMatchResponse
import models.{NormalMode, RegistrationType, UserAnswers, Utr}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.RegistrationTypePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.RegistrationTypeView

import scala.concurrent.Future

class RegistrationTypeControllerSpec extends SpecBase with MockitoSugar {

  lazy val registrationTypeRoute = routes.RegistrationTypeController.onPageLoad(NormalMode).url

  val formProvider = new RegistrationTypeFormProvider()
  val form = formProvider()

  "RegistrationType Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, registrationTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(RegistrationTypePage, RegistrationType.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, registrationTypeRoute)

        val view = application.injector.instanceOf[RegistrationTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(RegistrationType.values.head), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" - {

      "and store the registration response when the user has a UTR available to us" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockConnector = mock[RegistrationConnector]
        val mockTaxIdentifierProvider = mock[FakeTaxIdentifierProvider]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockConnector.register(any())(any())) thenReturn Future.successful(NoMatchResponse())
        when(mockTaxIdentifierProvider.taxIdentifier) thenReturn Some(Utr("123"))

        val application =
          applicationBuilder(userAnswers = None)
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[RegistrationConnector].toInstance(mockConnector),
              bind[FakeTaxIdentifierProvider].toInstance(mockTaxIdentifierProvider)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, registrationTypeRoute)
              .withFormUrlEncodedBody(("value", RegistrationType.values.head.toString))

          val expectedRegistrationRequest = OrganisationWithUtr("123", None)
          val answersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          
          val result = route(application, request).value
          
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual RegistrationTypePage.nextPage(NormalMode, emptyUserAnswers).url
          verify(mockConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
          verify(mockSessionRepository, times(1)).set(answersCaptor.capture())
          
          val savedAnswers = answersCaptor.getValue
          savedAnswers.taxIdentifier.value mustEqual Utr("123")
          savedAnswers.registrationResponse.value mustEqual NoMatchResponse()
        }
      }

      "and not make a registration call when the user does not have UTR available to us" in {

        val mockSessionRepository = mock[SessionRepository]
        val mockConnector = mock[RegistrationConnector]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository),
              bind[RegistrationConnector].toInstance(mockConnector)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, registrationTypeRoute)
              .withFormUrlEncodedBody(("value", RegistrationType.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual RegistrationTypePage.nextPage(NormalMode, emptyUserAnswers).url
          verify(mockConnector, never()).register(any())(any())
        }
      }
    }
    
    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, registrationTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[RegistrationTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }
  }
}
