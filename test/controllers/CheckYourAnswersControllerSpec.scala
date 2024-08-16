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
import connectors.SubscriptionConnector
import models.BusinessType.*
import models.pageviews.{CheckYourAnswersIndividualViewModel, CheckYourAnswersOrganisationViewModel}
import models.registration.Address
import models.registration.responses.{MatchResponseWithId, MatchResponseWithoutId}
import models.subscription.responses.{AlreadySubscribedResponse, SubscribedResponse, SubscriptionResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.*
import play.api.i18n.{DefaultLangsProvider, DefaultMessagesApi, Messages, MessagesApi}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{Configuration, Environment, Mode}
import repositories.SessionRepository
import views.html.{CheckYourAnswersIndividualView, CheckYourAnswersOrganisationView}

import java.io.File
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar {

  val testMessageMap =
    Map(
      "primaryContactName.checkYourAnswersLabel" -> "Contact name",
      "primaryContactName.change.hidden" -> "the name of the person or team we should contact",
      "site.change" -> "Change"
    )

  val messagesApi: MessagesApi = {
    val env = new Environment(new File("."), this.getClass.getClassLoader, Mode.Dev)
    val config = Configuration.reference ++ Configuration.from(Map("play.i18n.langs" -> Seq("en", "fr", "fr-CH")))
    val langs = new DefaultLangsProvider(config).get
    new DefaultMessagesApi(Map("en" -> testMessageMap), langs)
  }

  private implicit val messages: Messages = messagesApi.preferred(FakeRequest())
  private val anyIndividualType = Gen.oneOf(SoleTrader, Individual).sample.value
  private val anyOrganisationType = Gen.oneOf(LimitedCompany, Llp, Partnership, AssociationOrTrust).sample.value
  private val anyAddress = Address("line 1", None, None, None, None, "ZZ")

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" - {

      "for an individual" in {

        val answers = emptyUserAnswers
          .set(BusinessTypePage, anyIndividualType).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersIndividualView]
          val viewModel = CheckYourAnswersIndividualViewModel.apply(answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }

      "for an organisation when business type and name have been answered" in {

        val anyName = "name"
        val answers = emptyUserAnswers
          .set(BusinessNamePage, anyName).success.value
          .set(BusinessTypePage, anyOrganisationType).success.value
        
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersOrganisationView]
          val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }

      "for an organisation when the registration response is a match with Id containing an organisation name" in {

        val anyName = "name"
        val answers = emptyUserAnswers.copy(registrationResponse = Some(MatchResponseWithId("safe", anyAddress, Some(anyName))))
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersOrganisationView]
          val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }

      "for an organisation when the registration response is a match without Id containing " in {

        val answers =
          emptyUserAnswers.copy(registrationResponse = Some(MatchResponseWithoutId("safe")))
            .set(BusinessNameNoUtrPage, "business name").get

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersOrganisationView]
          val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery for a GET" - {

      "if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "if business type has not been answered" - {

        "and there is no registration response" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "and the registration response is a match response with Id with no organisation name" in {

          val answers = emptyUserAnswers.copy(registrationResponse = Some(MatchResponseWithId("safe", anyAddress, None)))
          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "and the registration response is not a match response with Id" in {

          val answers = emptyUserAnswers.copy(registrationResponse = Some(MatchResponseWithoutId("safe")))
          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }
  }
}