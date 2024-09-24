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

import config.AppConfig
import controllers.routes
import models.eacd.requests.GroupEnrolment
import models.requests.IdentifierRequest
import models.{Nino, Utr}
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction(override val authConnector: AuthConnector,
                                    appConfig: AppConfig,
                                    val parser: BodyParsers.Default,
                                    withDprsEnrollmentCheck: Boolean = false)
                                   (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(
      Retrievals.affinityGroup and
        Retrievals.credentialRole and
        Retrievals.internalId and
        Retrievals.nino and
        Retrievals.allEnrolments and
        Retrievals.groupIdentifier and
        Retrievals.credentials
    ) {
      case _ ~ _ ~ _ ~ _ ~ enrollments ~ _ ~ _ if withDprsEnrollmentCheck && hasDprsEnrolment(enrollments) =>
        Future.successful(Redirect(appConfig.manageFrontendUrl))

      case Some(Agent) ~ _ ~ _ ~ _ ~ _ ~ _ ~ _ =>
        Future.successful(Redirect(routes.CannotUseServiceAgentController.onPageLoad()))

      case Some(Organisation) ~ Some(Assistant) ~ _ ~ _ ~ _ ~ _ ~ _ =>
        Future.successful(Redirect(routes.CannotUseServiceAssistantController.onPageLoad()))

      case Some(Individual) ~ _ ~ Some(internalId) ~ maybeNino ~ _ ~ Some(groupIdentifier) ~ Some(credentials) =>
        val groupEnrolment = GroupEnrolment(credentials.providerId, groupIdentifier)
        block(IdentifierRequest(models.User(internalId, Some(groupEnrolment), maybeNino.map(Nino.apply)), request))

      case Some(Organisation) ~ _ ~ Some(internalId) ~ _ ~ enrolments ~ Some(groupIdentifier) ~ Some(credentials) =>
        val groupEnrolment = GroupEnrolment(credentials.providerId, groupIdentifier)
        block(IdentifierRequest(models.User(internalId, Some(groupEnrolment), getCtUtrEnrolment(enrolments)), request))

      case _ => Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
    } recover {
      case _: NoActiveSession => Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))
      case _: AuthorisationException => Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }

  private def getCtUtrEnrolment(enrolments: Enrolments): Option[Utr] =
    enrolments.getEnrolment("IR-CT")
      .flatMap { enrolment =>
        enrolment.identifiers
          .find(_.key == "UTR")
          .map(identifier => Utr(identifier.value))
      }

  private def hasDprsEnrolment(enrolments: Enrolments): Boolean =
    enrolments.getEnrolment("HMRC-DPRS")
      .flatMap(_.identifiers.find(_.key == "DPRSID"))
      .isDefined
}

trait IdentifierActionProvider {

  def apply(withDprsEnrollmentCheck: Boolean = true): IdentifierAction
}

class AuthenticatedIdentifierActionProvider @Inject()(authConnector: AuthConnector,
                                                      appConfig: AppConfig,
                                                      parser: BodyParsers.Default)
                                                     (implicit val executionContext: ExecutionContext)
  extends IdentifierActionProvider {

  def apply(withDprsEnrollmentCheck: Boolean = true) = new AuthenticatedIdentifierAction(
    withDprsEnrollmentCheck = withDprsEnrollmentCheck,
    authConnector = authConnector,
    appConfig = appConfig,
    parser = parser
  )
}