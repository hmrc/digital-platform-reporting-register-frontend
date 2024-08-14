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
import builders.InternationalAddressBuilder.anInternationalAddress
import builders.UserAnswersBuilder.aUserAnswers
import connectors.RegistrationConnector
import forms.InternationalAddressFormProvider
import models.registration.responses.NoMatchResponse
import models.{Country, IndividualName, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{DateOfBirthPage, IndividualNamePage, InternationalAddressPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.InternationalAddressView

import java.time.LocalDate
import scala.concurrent.Future

class InternationalAddressControllerSpec extends SpecBase with MockitoSugar {

  private val form = new InternationalAddressFormProvider()()
  private val country = Country.internationalCountries.head
  private val userAnswers = aUserAnswers.set(IndividualNamePage, IndividualName("first-name", "last-name")).success.value
    .set(DateOfBirthPage, LocalDate.of(2000, 1, 1)).success.value
    .set(InternationalAddressPage, anInternationalAddress).success.value

  private lazy val underTest = routes.InternationalAddressController.onPageLoad(NormalMode).url

  "InternationalAddress Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, underTest)
        val view = application.injector.instanceOf[InternationalAddressView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, underTest)
        val view = application.injector.instanceOf[InternationalAddressView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(anInternationalAddress), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val mockSessionRepository = mock[SessionRepository]
      val mockConnector = mock[RegistrationConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockConnector.register(any())(any())) thenReturn Future.successful(NoMatchResponse())

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[RegistrationConnector].toInstance(mockConnector),
      ).build()

      running(application) {
        val request = FakeRequest(POST, underTest)
          .withFormUrlEncodedBody(("line1", "Testing Lane"), ("city", "New York"), ("country", country.code))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual InternationalAddressPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, underTest).withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view = application.injector.instanceOf[InternationalAddressView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, underTest)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, underTest).withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"))
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
