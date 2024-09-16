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

package controllers.actions

import auth.Retrievals.*
import base.ControllerSpecBase
import com.google.inject.Inject
import config.AppConfig
import controllers.routes
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AuthActionSpec extends ControllerSpecBase {

  private val application = applicationBuilder(userAnswers = None).build()
  private val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
  private val appConfig = application.injector.instanceOf[AppConfig]
  private val emptyEnrolments = Enrolments(Set.empty)

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { request =>
      Results.Ok(s"${request.user.id}${request.user.taxIdentifier.map(_.value).getOrElse("")}")
    }
  }

  "Auth Action" - {
    "when the user hasn't logged in" - {
      "must redirect the user to log in " in {
        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, bodyParsers)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginUrl)
      }
    }

    "when the user's session has expired" - {
      "must redirect the user to log in " in {
        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, bodyParsers)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(appConfig.loginUrl)
      }
    }

    "when the user is an agent" - {
      "must redirect the user to the `cannot use service - agent` page" in {
        val authAction = new AuthenticatedIdentifierAction(new FakeAuthConnector(Some(Agent) ~ None ~ None ~ None ~ emptyEnrolments), appConfig, bodyParsers)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.CannotUseServiceAgentController.onPageLoad().url
      }
    }

    "when the user is an organisation assistant" - {
      "must redirect the user to the `cannot use service - assistant` page" in {
        val authAction = new AuthenticatedIdentifierAction(
          new FakeAuthConnector(Some(Organisation) ~ Some(Assistant) ~ None ~ None ~ emptyEnrolments),
          appConfig,
          bodyParsers
        )
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.CannotUseServiceAssistantController.onPageLoad().url
      }
    }

    "when the user is an organisation user" - {
      "must succeed" - {
        "when the user has a CT UTR enrolment" in {
          val enrolments = Enrolments(Set(Enrolment("IR-CT", Seq(EnrolmentIdentifier("UTR", " utr")), "activated", None)))
          val authAction = new AuthenticatedIdentifierAction(
            new FakeAuthConnector(Some(Organisation) ~ Some(User) ~ Some("internalId") ~ None ~ enrolments),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK
          contentAsString(result) mustEqual "internalId utr"
        }

        "when the user has no CT UTR enrolment" in {
          val authAction = new AuthenticatedIdentifierAction(
            new FakeAuthConnector(Some(Organisation) ~ Some(User) ~ Some("internalId") ~ None ~ emptyEnrolments),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK
          contentAsString(result) mustEqual "internalId"
        }
      }
    }

    "when the user is an individual" - {
      "must succeed" - {
        "when the user's NINO is attached to their auth record" in {
          val authAction = new AuthenticatedIdentifierAction(
            new FakeAuthConnector(Some(Individual) ~ None ~ Some("internalId") ~ Some(" nino") ~ emptyEnrolments),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK
          contentAsString(result) mustEqual "internalId nino"
        }

        "when the user's NINO is not attached to their auth record" in {
          val authAction = new AuthenticatedIdentifierAction(
            new FakeAuthConnector(Some(Individual) ~ None ~ Some("internalId") ~ None ~ emptyEnrolments),
            appConfig,
            bodyParsers
          )
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe OK
          contentAsString(result) mustEqual "internalId"
        }
      }
    }

    "when dprs enrollment check true passed" - {
      "when the user has no DPRS enrollment should continue" in {
        val enrolments = Enrolments(Set(Enrolment("IR-CT", Seq(EnrolmentIdentifier("UTR", " utr")), "activated", None)))
        val identifierActionProvider = new AuthenticatedIdentifierActionProvider(
          new FakeAuthConnector(Some(Organisation) ~ Some(User) ~ Some("internalId") ~ None ~ enrolments),
          appConfig,
          bodyParsers
        )
        val authAction = identifierActionProvider.apply(withDprsEnrollmentCheck = true)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest())

        status(result) mustBe OK
        contentAsString(result) mustEqual "internalId utr"
      }

      "when the user has DPRS enrollment should redirect to Manage Frontend" in {
        val enrolments = Enrolments(Set(Enrolment("HMRC-DPRS", Seq(EnrolmentIdentifier("DPRSID", " some-dprs-id")), "activated", None)))
        val identifierActionProvider = new AuthenticatedIdentifierActionProvider(
          new FakeAuthConnector(Some(Organisation) ~ Some(User) ~ Some("internalId") ~ None ~ enrolments),
          appConfig,
          bodyParsers
        )
        val authAction = identifierActionProvider.apply(withDprsEnrollmentCheck = true)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual appConfig.manageFrontendUrl
      }
    }

    "when dprs enrollment check false passed" - {
      "when the user has DPRS enrollment should redirect to Manage Frontend" in {
        val enrolments = Enrolments(Set(Enrolment("IR-CT", Seq(EnrolmentIdentifier("UTR", " utr")), "activated", None)))
        val identifierActionProvider = new AuthenticatedIdentifierActionProvider(
          new FakeAuthConnector(Some(Organisation) ~ Some(User) ~ Some("internalId") ~ None ~ enrolments),
          appConfig,
          bodyParsers
        )
        val authAction = identifierActionProvider.apply(withDprsEnrollmentCheck = true)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest())

        status(result) mustBe OK
        contentAsString(result) mustEqual "internalId utr"
      }
    }

    "when auth gives us back an unexpected set of retrievals" - {
      "must go to Unauthorised" in {
        val authAction = new AuthenticatedIdentifierAction(new FakeAuthConnector(None ~ None ~ None ~ None ~ emptyEnrolments), appConfig, bodyParsers)
        val controller = new Harness(authAction)
        val result = controller.onPageLoad()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual routes.UnauthorisedController.onPageLoad().url
      }
    }
  }
}

class FakeAuthConnector[T](value: T) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.fromTry(Try(value.asInstanceOf[A]))
}

class FakeFailingAuthConnector @Inject()(exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}
