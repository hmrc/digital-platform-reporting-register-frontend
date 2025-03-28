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
import forms.BusinessAddressFormProvider
import generators.Generators
import models.pageviews.BusinessAddressViewModel
import models.{BusinessAddress, DefaultCountriesList, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.BusinessAddressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.BusinessAddressView

import scala.concurrent.Future

class BusinessAddressControllerSpec extends ControllerSpecBase with MockitoSugar with Generators {

  private lazy val businessAddressRoute = routes.BusinessAddressController.onPageLoad(NormalMode).url

  private val form = new BusinessAddressFormProvider(new DefaultCountriesList)()

  "BusinessAddress Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, businessAddressRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[BusinessAddressView]
        val viewModel = BusinessAddressViewModel(NormalMode, aUserAnswers, form)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val address = arbitraryBusinessAddress.arbitrary.sample.get
      val userAnswers = anEmptyAnswer.set(BusinessAddressPage, address).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, businessAddressRoute)
        val view = application.injector.instanceOf[BusinessAddressView]
        val result = route(application, request).value
        val viewModel = BusinessAddressViewModel(NormalMode, aUserAnswers, form.fill(address))

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
        val request = FakeRequest(POST, businessAddressRoute)
          .withFormUrlEncodedBody(("addressLine1", "value 1"), ("city", "value 2"), ("postalCode", "postcode"), ("country", "US"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual BusinessAddressPage.nextPage(NormalMode, anEmptyAnswer).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(POST, businessAddressRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view = application.injector.instanceOf[BusinessAddressView]
        val result = route(application, request).value
        val viewModel = BusinessAddressViewModel(NormalMode, aUserAnswers, boundForm)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, businessAddressRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, businessAddressRoute)
          .withFormUrlEncodedBody(("addressLine1", "value 1"), ("addressLine2", "value 2"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
