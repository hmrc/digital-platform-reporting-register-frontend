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
import forms.ClaimEnrolmentFormProvider
import models.ClaimEnrolmentDetails
import org.scalacheck.Gen
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.ClaimEnrolmentView

import scala.concurrent.Future

class ClaimEnrolmentControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val formProvider = new ClaimEnrolmentFormProvider()
  private val form = formProvider()

  private lazy val claimEnrolmentRoute = routes.ClaimEnrolmentController.onPageLoad().url

  private val utr = Gen.listOfN(10, Gen.numChar).map(_.mkString).sample.value
  private val businessName = "businessName"

  "ClaimEnrolment Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, claimEnrolmentRoute)

        val view = application.injector.instanceOf[ClaimEnrolmentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }
    
    "must redirect to index when valid data is submitted" in {
      
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, claimEnrolmentRoute)
            .withFormUrlEncodedBody(("utr", utr), ("businessName", businessName))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, claimEnrolmentRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ClaimEnrolmentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}
