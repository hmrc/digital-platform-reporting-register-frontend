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

import base.ControllerSpecBase
import builders.UserAnswersBuilder.{aUserAnswers, anEmptyAnswer}
import connectors.RegistrationConnector
import forms.BusinessNameFormProvider
import models.pageviews.BusinessNameViewModel
import models.registration.requests.{OrganisationDetails, OrganisationWithUtr}
import models.registration.responses.NoMatchResponse
import models.{BusinessType, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessNamePage, BusinessTypePage, UtrPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.BusinessNameView

import scala.concurrent.Future

class BusinessNameControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val formProvider = new BusinessNameFormProvider()
  private val form = formProvider()

  private lazy val businessNameRoute = routes.BusinessNameController.onPageLoad(NormalMode).url

  "BusinessName Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, businessNameRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[BusinessNameView]
        val viewModel = BusinessNameViewModel(NormalMode, aUserAnswers, form)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = anEmptyAnswer.set(BusinessNamePage, "name").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, businessNameRoute)
        val view = application.injector.instanceOf[BusinessNameView]
        val result = route(application, request).value
        val viewModel = BusinessNameViewModel(NormalMode, aUserAnswers, form.fill("name"))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to the next page and store the registration result when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockConnector = mock[RegistrationConnector]

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockConnector.register(any())(any())).thenReturn(Future.successful(NoMatchResponse()))

      val baseAnswers =
        anEmptyAnswer
          .set(UtrPage, "123").success.value
          .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
          .copy(registrationResponse = Some(NoMatchResponse()))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[RegistrationConnector].toInstance(mockConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, businessNameRoute)
            .withFormUrlEncodedBody(("value", "name"))

        val expectedRegistrationRequest = OrganisationWithUtr("123", Some(OrganisationDetails("name", BusinessType.LimitedCompany)))

        val expectedAnswers =
          baseAnswers
            .set(BusinessNamePage, "name").success.value
            .copy(registrationResponse = Some(NoMatchResponse()))

        val answersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual BusinessNamePage.nextPage(NormalMode, expectedAnswers).url
        verify(mockConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
        verify(mockSessionRepository, times(1)).set(answersCaptor.capture())

        val savedAnswers = answersCaptor.getValue
        savedAnswers.registrationResponse.value mustEqual NoMatchResponse()
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request =
          FakeRequest(POST, businessNameRoute)
            .withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[BusinessNameView]
        val result = route(application, request).value
        val viewModel = BusinessNameViewModel(NormalMode, aUserAnswers, boundForm)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, businessNameRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, businessNameRoute)
            .withFormUrlEncodedBody(("value", "name"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return message saying unable to build registration request, path missing: utr" in {
      val baseAnswers =
        anEmptyAnswer
          .set(BusinessTypePage, BusinessType.LimitedCompany).success.value

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, businessNameRoute)
            .withFormUrlEncodedBody(("value", "name"))

        val result = route(application, request).value.failed.futureValue.getMessage

        result mustEqual "Unable to build registration request, path(s) missing: /utr"
      }
    }
  }
}
