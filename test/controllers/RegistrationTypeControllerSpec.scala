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
import forms.RegistrationTypeFormProvider
import models.{NormalMode, RegistrationType}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.RegistrationTypeView


class RegistrationTypeControllerSpec extends ControllerSpecBase with MockitoSugar {

  private lazy val registrationTypeRoute = routes.RegistrationTypeController.onPageLoad(NormalMode).url

  private val form = new RegistrationTypeFormProvider()()

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

    "must redirect to the next page when valid data is submitted" - {
      "and platform operator is selected" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, registrationTypeRoute)
            .withFormUrlEncodedBody(("value", RegistrationType.PlatformOperator.toString))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.RegistrationTypeContinueController.platformOperator().url
        }
      }
      "and third party is selected" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(POST, registrationTypeRoute)
            .withFormUrlEncodedBody(("value", RegistrationType.ThirdParty.toString))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.RegistrationTypeContinueController.thirdParty().url
        }
      }

    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(POST, registrationTypeRoute)
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
