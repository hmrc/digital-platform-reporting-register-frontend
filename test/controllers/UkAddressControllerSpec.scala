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
import forms.UkAddressFormProvider
import models.{Country, NormalMode, UkAddress}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.UkAddressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.UkAddressView

import scala.concurrent.Future

class UkAddressControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val formProvider = new UkAddressFormProvider()
  private val form = formProvider()
  private val country = Country.UnitedKingdom
  private lazy val ukAddressRoute = routes.UkAddressController.onPageLoad(NormalMode).url
  private val validUkAddress = UkAddress(
    "line 1",
    None,
    "town",
    Some("county"),
    "AA1 1AA",
    country
  )
  private val userAnswers = anEmptyAnswer.set(UkAddressPage, validUkAddress).success.value

  private def getParam(key: String, value: Option[String]) =
    Seq(value.map(key -> _))

  "UkAddress Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, ukAddressRoute)
        val view = application.injector.instanceOf[UkAddressView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, ukAddressRoute)
        val view = application.injector.instanceOf[UkAddressView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validUkAddress), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(anEmptyAnswer))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val params = getParam("line1", Some(validUkAddress.line1)) ++
          getParam("line2", validUkAddress.line2) ++
          getParam("town", Some(validUkAddress.town)) ++
          getParam("county", validUkAddress.county) ++
          getParam("postCode", Some(validUkAddress.postCode)) ++
          getParam("country", Some(validUkAddress.country.code))
        val request = FakeRequest(POST, ukAddressRoute)
          .withFormUrlEncodedBody(params.flatten *)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual UkAddressPage.nextPage(NormalMode, anEmptyAnswer).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(POST, ukAddressRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view = application.injector.instanceOf[UkAddressView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, ukAddressRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, ukAddressRoute)
          .withFormUrlEncodedBody(("line1", "value 1"), ("town", "value 2"), ("postCode", "AA1 1AA"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
