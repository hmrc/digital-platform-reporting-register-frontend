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
import builders.AddressBuilder.anAddress
import builders.UserAnswersBuilder.{aUserAnswers, anEmptyAnswer}
import helpers.UserAnswerHelper
import models.pageviews.BusinessDetailsMatchOtherViewModel
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.BusinessDetailsMatchOtherView

class BusinessDetailsMatchOtherControllerSpec extends ControllerSpecBase with UserAnswerHelper {

  "BusinessDetailsMatchOther Controller" - {
    "must return OK and the correct view for a GET" in {
      val userAnswers = aUserAnswers.withBusiness("some-business-name", anAddress)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.BusinessDetailsMatchOtherController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[BusinessDetailsMatchOtherView]
        val viewModel = BusinessDetailsMatchOtherViewModel(userAnswers).get

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, routes.BusinessDetailsMatchOtherController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
