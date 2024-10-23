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
import forms.CanPhoneIndividualFormProvider
import models.NormalMode
import models.pageviews.CanPhoneIndividualViewModel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.CanPhoneIndividualPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.CanPhoneIndividualView

import scala.concurrent.Future

class CanPhoneIndividualControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val form = new CanPhoneIndividualFormProvider()()
  private lazy val canPhoneIndividualRoute = routes.CanPhoneIndividualController.onPageLoad(NormalMode).url

  "CanPhoneIndividual Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, canPhoneIndividualRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CanPhoneIndividualView]
        val viewModel = CanPhoneIndividualViewModel(NormalMode, aUserAnswers, form)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = anEmptyAnswer.set(CanPhoneIndividualPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, canPhoneIndividualRoute)
        val view = application.injector.instanceOf[CanPhoneIndividualView]
        val result = route(application, request).value
        val viewModel = CanPhoneIndividualViewModel(NormalMode, aUserAnswers, form.fill(true))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(POST, canPhoneIndividualRoute)
          .withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        val expectedAnswers = anEmptyAnswer.set(CanPhoneIndividualPage, true).success.value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CanPhoneIndividualPage.nextPage(NormalMode, expectedAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(POST, canPhoneIndividualRoute)
          .withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[CanPhoneIndividualView]
        val result = route(application, request).value
        val viewModel = CanPhoneIndividualViewModel(NormalMode, aUserAnswers, boundForm)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, canPhoneIndividualRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, canPhoneIndividualRoute)
          .withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
