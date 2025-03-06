/*
 * Copyright 2025 HM Revenue & Customs
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
import models.BusinessType.{AssociationOrTrust, LimitedCompany, SoleTrader}
import models.pageviews.CannotUseServiceThirdPartyIndividualViewModel
import pages.BusinessTypePage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.CannotUseServiceThirdPartyIndividualView

class CannotUseServiceThirdPartyIndividualControllerSpec extends ControllerSpecBase {

  "CannotUseServiceThirdPartyIndividualController Controller" - {
    "must return OK and the correct view for a GET when" - {
      "BusinessType is LimitedCompany" in {
        val userAnswers = anEmptyAnswer.set(BusinessTypePage, LimitedCompany).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CannotUseServiceThirdPartyIndividualController.onPageLoad().url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[CannotUseServiceThirdPartyIndividualView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(CannotUseServiceThirdPartyIndividualViewModel(LimitedCompany))(request, messages(application)).toString
        }
      }

      "BusinessType is AssociationOrTrust" in {
        val userAnswers = anEmptyAnswer.set(BusinessTypePage, AssociationOrTrust).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CannotUseServiceThirdPartyIndividualController.onPageLoad().url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[CannotUseServiceThirdPartyIndividualView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(CannotUseServiceThirdPartyIndividualViewModel(AssociationOrTrust))(request, messages(application)).toString
        }
      }
    }

    "must redirect to start of journey userAnswers do not contain BusinessType" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CannotUseServiceThirdPartyIndividualController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to start of journey when BusinessType is not LimitedCompany or AssociationOrTrust" in {
      val userAnswers = anEmptyAnswer.set(BusinessTypePage, SoleTrader).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CannotUseServiceThirdPartyIndividualController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
