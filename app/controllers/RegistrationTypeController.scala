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
import controllers.actions.*
import forms.RegistrationTypeFormProvider
import models.registration.requests.OrganisationWithUtr
import models.registration.responses.RegistrationResponse
import models.{Mode, UserAnswers, Utr}
import pages.RegistrationTypePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.RegistrationTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationTypeController @Inject()(sessionRepository: SessionRepository,
                                           identify: IdentifierActionProvider,
                                           getData: DataRetrievalAction,
                                           formProvider: RegistrationTypeFormProvider,
                                           view: RegistrationTypeView,
                                           registrationConnector: RegistrationConnector)
                                          (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify() andThen getData) { implicit request =>
    val answers = request.userAnswers
      .getOrElse(UserAnswers(request.user))
      .get(RegistrationTypePage)

    val preparedForm = answers match {
      case None => formProvider()
      case Some(value) => formProvider().fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify() andThen getData).async { implicit request =>
    formProvider().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
      value => {
        val baseAnswers = request.userAnswers.getOrElse(UserAnswers(request.user))

        for {
          matchResult <- matchIfNecessary(baseAnswers)
          answers = baseAnswers.copy(registrationResponse = matchResult)
          updatedAnswers <- Future.fromTry(answers.set(RegistrationTypePage, value))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(RegistrationTypePage.nextPage(mode, updatedAnswers))
      }
    )
  }

  private def matchIfNecessary(answers: UserAnswers)
                              (implicit request: Request[?]): Future[Option[RegistrationResponse]] = answers.user.taxIdentifier
    .map {
      case Utr(utr) => registrationConnector.register(OrganisationWithUtr(utr, None)).map(Some(_))
      case _ => Future.successful(None)
    }.getOrElse(Future.successful(None))
}
