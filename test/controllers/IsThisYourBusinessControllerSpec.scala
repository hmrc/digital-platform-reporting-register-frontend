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
import builders.AddressBuilder.anAddress
import builders.UserAnswersBuilder.anEmptyAnswer
import forms.IsThisYourBusinessFormProvider
import helpers.UserAnswerHelper
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.IsThisYourBusinessPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.IsThisYourBusinessView

import scala.concurrent.Future

class IsThisYourBusinessControllerSpec extends SpecBase with MockitoSugar with UserAnswerHelper {

  private val formProvider = new IsThisYourBusinessFormProvider()
  private val form = formProvider()
  private lazy val isThisYourBusinessRoute = routes.IsThisYourBusinessController.onPageLoad(NormalMode).url
  private val businessName = "Coca-Cola"
  private val businessAddress = anAddress

  "IsThisYourBusiness Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers = anEmptyAnswer.withBusiness(businessName, businessAddress)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, isThisYourBusinessRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsThisYourBusinessView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, businessName, businessAddress, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = anEmptyAnswer.withBusiness(businessName, businessAddress)
        .set(IsThisYourBusinessPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, isThisYourBusinessRoute)

        val view = application.injector.instanceOf[IsThisYourBusinessView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), businessName, businessAddress, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val userAnswers = anEmptyAnswer.withBusiness(businessName, businessAddress)

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, isThisYourBusinessRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val expectedAnswers = anEmptyAnswer.set(IsThisYourBusinessPage, true).success.value
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual IsThisYourBusinessPage.nextPage(NormalMode, expectedAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = anEmptyAnswer.withBusiness(businessName, businessAddress)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, isThisYourBusinessRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsThisYourBusinessView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, businessName, businessAddress, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, isThisYourBusinessRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no business name is found" in {

      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, isThisYourBusinessRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, isThisYourBusinessRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no business name is found" in {

      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request =
          FakeRequest(POST, isThisYourBusinessRoute)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
