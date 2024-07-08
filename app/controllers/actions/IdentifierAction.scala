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

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.Utr
import models.requests.IdentifierRequest
import play.api.mvc.Results.*
import play.api.mvc.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default
                                             )
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(
      Retrievals.affinityGroup and
        Retrievals.credentialRole and
        Retrievals.internalId and
        Retrievals.nino and
        Retrievals.allEnrolments
    ) {
      case Some(Agent) ~ _ ~ _ ~ _ ~ _ =>
        Future.successful(Redirect(routes.CannotUseServiceAgentController.onPageLoad()))
        
      case Some(Organisation) ~ Some(Assistant) ~ _ ~ _ ~ _ =>
        Future.successful(Redirect(routes.CannotUseServiceAssistantController.onPageLoad()))

      case Some(Individual) ~ _ ~ _ ~ _ ~ _ =>
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
        
      case Some(Organisation) ~ _ ~ Some(internalId) ~ _ ~ enrolments =>
        block(IdentifierRequest(request, internalId, getCtUtrEnrolment(enrolments)))
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }
  
  private def getCtUtrEnrolment(enrolments: Enrolments): Option[Utr] =
    enrolments.getEnrolment("IR-CT")
      .flatMap { enrolment =>
        enrolment.identifiers
          .find(_.key == "UTR")
          .map(identifier => Utr(identifier.value))
      }
}
