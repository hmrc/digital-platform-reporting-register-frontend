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
import builders.UserAnswersBuilder.anEmptyAnswer
import forms.SecondaryContactEmailAddressFormProvider
import models.NormalMode
import models.pageviews.SecondaryContactEmailAddressViewModel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{SecondaryContactEmailAddressPage, SecondaryContactNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.SecondaryContactEmailAddressView

import scala.concurrent.Future

class SecondaryContactEmailAddressControllerSpec extends ControllerSpecBase with MockitoSugar {

  private lazy val secondaryContactEmailAddressRoute = routes.SecondaryContactEmailAddressController.onPageLoad(NormalMode).url
  private val anyName = "name"
  private val baseAnswers = anEmptyAnswer.set(SecondaryContactNamePage, anyName).success.value
  private val form = new SecondaryContactEmailAddressFormProvider()(anyName)

  "SecondaryContactEmailAddress Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, secondaryContactEmailAddressRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[SecondaryContactEmailAddressView]
        val viewModel = SecondaryContactEmailAddressViewModel(NormalMode, baseAnswers, form, anyName)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = baseAnswers.set(SecondaryContactEmailAddressPage, "foo@example.com").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, secondaryContactEmailAddressRoute)
        val view = application.injector.instanceOf[SecondaryContactEmailAddressView]
        val result = route(application, request).value
        val viewModel = SecondaryContactEmailAddressViewModel(NormalMode, baseAnswers, form.fill("foo@example.com"), anyName)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val request = FakeRequest(POST, secondaryContactEmailAddressRoute)
          .withFormUrlEncodedBody(("value", "foo@example.com"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual SecondaryContactEmailAddressPage.nextPage(NormalMode, baseAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, secondaryContactEmailAddressRoute)
          .withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[SecondaryContactEmailAddressView]
        val result = route(application, request).value
        val viewModel = SecondaryContactEmailAddressViewModel(NormalMode, baseAnswers, boundForm, anyName)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, secondaryContactEmailAddressRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, secondaryContactEmailAddressRoute)
          .withFormUrlEncodedBody(("value", "foo@example.com"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
