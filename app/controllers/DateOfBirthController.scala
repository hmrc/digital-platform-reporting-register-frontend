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
import forms.DateOfBirthFormProvider
import models.registration.requests.IndividualWithNino
import models.registration.responses.RegistrationResponse
import models.{Mode, UserAnswers}
import pages.{DateOfBirthPage, HasNinoPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.DateOfBirthView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateOfBirthController @Inject()(sessionRepository: SessionRepository,
                                      identify: IdentifierActionProvider,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: DateOfBirthFormProvider,
                                      view: DateOfBirthView,
                                      registrationConnector: RegistrationConnector)
                                     (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(DateOfBirthPage) match {
      case None => formProvider()
      case Some(value) => formProvider().fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData).async { implicit request =>
    formProvider().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
      value =>
        val hasNino = request.userAnswers.get(HasNinoPage).getOrElse(false)
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(DateOfBirthPage, value))
          fullAnswers <- if (hasNino) registerAndSave(updatedAnswers) else Future.successful(updatedAnswers)
          _ <- sessionRepository.set(fullAnswers)
        } yield Redirect(DateOfBirthPage.nextPage(mode, fullAnswers))
    )
  }

  private def registerAndSave(answers: UserAnswers)(implicit request: Request[_]): Future[UserAnswers] =
    IndividualWithNino.build(answers).fold(
      errors => Future.failed(Exception(s"Unable to build registration request, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}")),
      details => registrationConnector.register(details).map {
        response =>
          answers.copy(
            registrationResponse = Some(response)
          )
      }
    )
}
