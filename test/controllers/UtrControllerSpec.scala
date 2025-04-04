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
import forms.UtrFormProvider
import models.BusinessType.{AssociationOrTrust, LimitedCompany, Llp, Partnership, SoleTrader}
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessTypePage, UtrPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.{UtrCorporationTaxView, UtrPartnershipView, UtrSelfAssessmentView}

import scala.concurrent.Future

class UtrControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val formProvider = new UtrFormProvider()
  private lazy val utrRoute = routes.UtrController.onPageLoad(NormalMode).url
  private val utr = "1234567890"

  "Utr Controller" - {
    "must return OK and the correct view for a GET" - {
      val form = formProvider("utrCorporationTax")

      for (businessType <- Seq(LimitedCompany, AssociationOrTrust)) {
        s"for a ${businessType.toString}" in {
          val answers = anEmptyAnswer.set(BusinessTypePage, businessType).success.value
          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, utrRoute)
            val result = route(application, request).value
            val view = application.injector.instanceOf[UtrCorporationTaxView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
          }
        }
      }

      for (businessType <- Seq(Llp, Partnership)) {

        val form = formProvider("utrPartnership")

        s"for a ${businessType.toString}" in {

          val answers = anEmptyAnswer.set(BusinessTypePage, businessType).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, utrRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[UtrPartnershipView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
          }
        }
      }

      "for a Sole Trader" in {

        val form = formProvider("utrSelfAssessment")

        val answers = anEmptyAnswer.set(BusinessTypePage, SoleTrader).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, utrRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[UtrSelfAssessmentView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val form = formProvider("utrSelfAssessment")

      val answers = anEmptyAnswer
        .set(BusinessTypePage, SoleTrader).success.value
        .set(UtrPage, utr).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, utrRoute)

        val view = application.injector.instanceOf[UtrSelfAssessmentView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(utr), NormalMode)(request, messages(application)).toString
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
        val request =
          FakeRequest(POST, utrRoute)
            .withFormUrlEncodedBody(("value", utr))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual UtrPage.nextPage(NormalMode, anEmptyAnswer).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val form = formProvider("utrSelfAssessment")

      val answers = anEmptyAnswer.set(BusinessTypePage, SoleTrader).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request =
          FakeRequest(POST, utrRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[UtrSelfAssessmentView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, utrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, utrRoute)
            .withFormUrlEncodedBody(("value", utr))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
