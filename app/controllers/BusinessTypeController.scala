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
import controllers.actions._
import forms.BusinessTypeFormProvider
import models.registration.requests.OrganisationWithUtr
import models.registration.responses.RegistrationResponse
import models.{Mode, NormalMode, RegistrationType, UserAnswers, Utr}
import pages.{BusinessTypePage, RegistrationTypePage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.BusinessTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessTypeController @Inject()(sessionRepository: SessionRepository,
                                       identify: IdentifierActionProvider,
                                       identifyPlatformOperator: IdentifierPlatformOperatorActionProvider,
                                       identifyThirdParty: IdentifierThirdPartyActionProvider,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: BusinessTypeFormProvider,
                                       registrationConnector: RegistrationConnector,
                                       view: BusinessTypeView)
                                      (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with AnswerExtractor {

  def platformOperator(): Action[AnyContent] = (identifyPlatformOperator() andThen getData).async { implicit request =>
    val baseAnswers = request.userAnswers.getOrElse(UserAnswers(request.user))
    for {
      matchResult <- matchIfNecessary(baseAnswers)
      answers = baseAnswers.copy(registrationResponse = matchResult)
      updatedAnswers <- Future.fromTry(answers.set(RegistrationTypePage, RegistrationType.PlatformOperator))
      _ <- sessionRepository.set(updatedAnswers)
    } yield Redirect(RegistrationTypePage.nextPage(NormalMode, updatedAnswers))
  }

  def thirdParty(): Action[AnyContent] = (identifyThirdParty() andThen getData).async { implicit request =>
    val baseAnswers = request.userAnswers.getOrElse(UserAnswers(request.user))
    for {
      matchResult <- matchIfNecessary(baseAnswers)
      answers = baseAnswers.copy(registrationResponse = matchResult)
      updatedAnswers <- Future.fromTry(answers.set(RegistrationTypePage, RegistrationType.ThirdParty))
      _ <- sessionRepository.set(updatedAnswers)
    } yield Redirect(RegistrationTypePage.nextPage(NormalMode, updatedAnswers))
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData) { implicit request =>
    getAnswer(RegistrationTypePage) { registrationType =>
      val preparedForm = request.userAnswers.get(BusinessTypePage) match {
        case None => formProvider(registrationType)
        case Some(value) => formProvider(registrationType).fill(value)
      }

      Ok(view(preparedForm, mode, registrationType))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData).async { implicit request =>
    getAnswerAsync(RegistrationTypePage) { registrationType =>
      formProvider(registrationType).bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, registrationType))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessTypePage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(BusinessTypePage.nextPage(mode, updatedAnswers))
      )
    }
  }

  private def matchIfNecessary(answers: UserAnswers)
                              (implicit request: Request[?]): Future[Option[RegistrationResponse]] = answers.user.taxIdentifier
    .map {
      case Utr(utr) => registrationConnector.register(OrganisationWithUtr(utr, None)).map(Some(_))
      case _ => Future.successful(None)
    }.getOrElse(Future.successful(None))


}
