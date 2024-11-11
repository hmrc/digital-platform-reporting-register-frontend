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

import connectors.RegistrationConnector
import controllers.actions.{DataRetrievalAction, IdentifierActionProvider}
import models.registration.requests.OrganisationWithUtr
import models.registration.responses.RegistrationResponse
import models.{NormalMode, RegistrationType, UserAnswers, Utr}
import pages.RegistrationTypePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationTypeContinueController @Inject()(sessionRepository: SessionRepository,
                                                   identify: IdentifierActionProvider,
                                                   getData: DataRetrievalAction,
                                                   registrationConnector: RegistrationConnector)
                                                  (implicit mcc: MessagesControllerComponents,
                                                           ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def platformOperator(): Action[AnyContent] = (identify() andThen getData).async { implicit request =>
    val baseAnswers = request.userAnswers.getOrElse(UserAnswers(request.user))
    for {
      matchResult <- matchIfNecessary(baseAnswers)
      answers = baseAnswers.copy(registrationResponse = matchResult)
      updatedAnswers <- Future.fromTry(answers.set(RegistrationTypePage, RegistrationType.PlatformOperator))
      _ <- sessionRepository.set(updatedAnswers)
    } yield Redirect(RegistrationTypePage.nextPage(NormalMode, updatedAnswers))
  }

  def thirdParty(): Action[AnyContent] = (identify() andThen getData).async { implicit request =>
    val baseAnswers = request.userAnswers.getOrElse(UserAnswers(request.user))
    for {
      matchResult <- matchIfNecessary(baseAnswers)
      answers = baseAnswers.copy(registrationResponse = matchResult)
      updatedAnswers <- Future.fromTry(answers.set(RegistrationTypePage, RegistrationType.ThirdParty))
      _ <- sessionRepository.set(updatedAnswers)
    } yield Redirect(RegistrationTypePage.nextPage(NormalMode, updatedAnswers))
  }

  private def matchIfNecessary(answers: UserAnswers)
                              (implicit request: Request[?]): Future[Option[RegistrationResponse]] = answers.user.taxIdentifier
    .map {
      case Utr(utr) => registrationConnector.register(OrganisationWithUtr(utr, None)).map(Some(_))
      case _ => Future.successful(None)
    }.getOrElse(Future.successful(None))

}
