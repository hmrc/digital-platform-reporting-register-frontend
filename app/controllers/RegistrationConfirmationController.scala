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

import config.FrontendAppConfig
import controllers.actions.*
import forms.RegistrationConfirmationFormProvider
import models.Mode
import models.pageviews.RegistrationConfirmationViewModel
import pages.RegistrationConfirmationPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.RegistrationConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConfirmationController @Inject()(sessionRepository: SessionRepository,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: RegistrationConfirmationFormProvider,
                                                   appConfig: FrontendAppConfig,
                                                   view: RegistrationConfirmationView)
                                                  (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    showPage(
      RegistrationConfirmationViewModel(mode, request.userAnswers, formProvider(), appConfig.isPrivateBeta),
      model => Ok(view(model))
    )
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    formProvider().bindFromRequest().fold(
      formWithErrors => Future.successful(showPage(
        RegistrationConfirmationViewModel(mode, request.userAnswers, formWithErrors, appConfig.isPrivateBeta),
        model => BadRequest(view(model))
      )),
      value =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(RegistrationConfirmationPage, value))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(RegistrationConfirmationPage.nextPage(mode, updatedAnswers))
    )
  }

  private def showPage(model: Option[RegistrationConfirmationViewModel], page: RegistrationConfirmationViewModel => Result) =
    model match {
      case Some(value) => page(value)
      case None => Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
}
