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
import forms.HasUtrFormProvider
import models.BusinessType.*
import models.{BusinessType, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessTypePage, HasUtrPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.{HasUtrCorporationTaxView, HasUtrPartnershipView, HasUtrSelfAssessmentView}

import scala.concurrent.Future

class HasUtrControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new HasUtrFormProvider()
  val form = formProvider()

  lazy val hasUtrRoute = routes.HasUtrController.onPageLoad(NormalMode).url

  "HasUtr Controller" - {

    "must return OK and the correct view for a GET" - {

      for (businessType <- Seq(LimitedCompany, AssociationOrTrust)) {

        s"for a ${businessType.toString}" in {

          val answers = emptyUserAnswers.set(BusinessTypePage, businessType).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, hasUtrRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[HasUtrCorporationTaxView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
          }
        }
      }

      for (businessType <- Seq(Llp, Partnership)) {

        s"for a ${businessType.toString}" in {

          val answers = emptyUserAnswers.set(BusinessTypePage, businessType).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, hasUtrRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[HasUtrPartnershipView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
          }
        }
      }

      "for a Sole Trader" in {

        val answers = emptyUserAnswers.set(BusinessTypePage, SoleTrader).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, hasUtrRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[HasUtrSelfAssessmentView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery for a GET for an Individual" in {

      val answers = emptyUserAnswers.set(BusinessTypePage, Individual).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, hasUtrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val answers = emptyUserAnswers.set(BusinessTypePage, BusinessType.LimitedCompany).success.value

      val userAnswers = answers.set(HasUtrPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, hasUtrRoute)

        val view = application.injector.instanceOf[HasUtrCorporationTaxView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val answers = emptyUserAnswers.set(BusinessTypePage, BusinessType.LimitedCompany).success.value

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, hasUtrRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" - {

      for (businessType <- Seq(LimitedCompany, AssociationOrTrust)) {

        s"for a ${businessType.toString}" in {

          val answers = emptyUserAnswers.set(BusinessTypePage, businessType).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request =
              FakeRequest(POST, hasUtrRoute)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[HasUtrCorporationTaxView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
          }
        }
      }

      for (businessType <- Seq(Llp, Partnership)) {

        s"for a ${businessType.toString}" in {

          val answers = emptyUserAnswers.set(BusinessTypePage, businessType).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request =
              FakeRequest(POST, hasUtrRoute)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[HasUtrPartnershipView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
          }
        }
      }

      "for a SoleTrader" in {

        val answers = emptyUserAnswers.set(BusinessTypePage, SoleTrader).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request =
            FakeRequest(POST, hasUtrRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[HasUtrSelfAssessmentView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery for a POST for an Individual" in {

      val answers = emptyUserAnswers.set(BusinessTypePage, Individual).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request =
          FakeRequest(POST, hasUtrRoute)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, hasUtrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if business type has not been answered" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, hasUtrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, hasUtrRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if business type has not been answered" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, hasUtrRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
