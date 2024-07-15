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
import forms.SoleTraderNameFormProvider
import models.registration.requests.IndividualWithUtr
import models.registration.responses.RegistrationResponse

import javax.inject.Inject
import models.{Mode, UserAnswers}
import pages.SoleTraderNamePage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.SoleTraderNameView

import scala.concurrent.{ExecutionContext, Future}

class SoleTraderNameController @Inject()(sessionRepository: SessionRepository,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: SoleTraderNameFormProvider,
                                         view: SoleTraderNameView,
                                         registrationConnector: RegistrationConnector)
                                        (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(SoleTraderNamePage) match {
      case None => formProvider()
      case Some(value) => formProvider().fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>

    formProvider().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
      value =>
        for {
          updatedAnswers   <- Future.fromTry(request.userAnswers.set(SoleTraderNamePage, value))
          registerResponse <- register(updatedAnswers)
          fullAnswers      = updatedAnswers.copy(registrationResponse = Some(registerResponse))
          _                <- sessionRepository.set(fullAnswers)
        } yield Redirect(SoleTraderNamePage.nextPage(mode, fullAnswers))
    )
  }

  private def register(answers: UserAnswers)(implicit request: Request[_]): Future[RegistrationResponse] =
    IndividualWithUtr.build(answers)
      .fold(
        errors => Future.failed(Exception(s"Unable to build registration request, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}")),
        details => registrationConnector.register(details)
      )
}
