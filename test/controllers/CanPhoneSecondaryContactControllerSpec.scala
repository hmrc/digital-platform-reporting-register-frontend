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
import forms.CanPhoneSecondaryContactFormProvider
import models.NormalMode
import models.pageviews.CanPhoneSecondaryContactViewModel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{CanPhoneSecondaryContactPage, SecondaryContactNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.CanPhoneSecondaryContactView

import scala.concurrent.Future

class CanPhoneSecondaryContactControllerSpec extends SpecBase with MockitoSugar {

  private lazy val canPhoneSecondaryContactRoute = routes.CanPhoneSecondaryContactController.onPageLoad(NormalMode).url
  private val anyName = "name"
  private val anyBoolean = true
  private val baseAnswers =
    emptyUserAnswers
      .set(SecondaryContactNamePage, anyName).success.value
      .set(CanPhoneSecondaryContactPage, anyBoolean).success.value
  private val form = new CanPhoneSecondaryContactFormProvider()(anyName)

  "CanPhoneSecondaryContact Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, canPhoneSecondaryContactRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CanPhoneSecondaryContactView]
        val viewModel = CanPhoneSecondaryContactViewModel(NormalMode, baseAnswers, form, anyName)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = baseAnswers.set(CanPhoneSecondaryContactPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, canPhoneSecondaryContactRoute)
        val view = application.injector.instanceOf[CanPhoneSecondaryContactView]
        val result = route(application, request).value
        val viewModel = CanPhoneSecondaryContactViewModel(NormalMode, baseAnswers, form.fill(true), anyName)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(POST, canPhoneSecondaryContactRoute)
          .withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CanPhoneSecondaryContactPage.nextPage(NormalMode, baseAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, canPhoneSecondaryContactRoute)
          .withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[CanPhoneSecondaryContactView]
        val result = route(application, request).value
        val viewModel = CanPhoneSecondaryContactViewModel(NormalMode, baseAnswers, boundForm, anyName)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, canPhoneSecondaryContactRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, canPhoneSecondaryContactRoute)
          .withFormUrlEncodedBody(("value", "true"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
