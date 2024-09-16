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

import config.AppConfig
import controllers.actions.*
import forms.RegistrationConfirmationFormProvider
import models.Mode
import models.pageviews.RegistrationConfirmationViewModel
import models.subscription.responses.SubscribedResponse
import pages.RegistrationConfirmationPage
import play.api.i18n.I18nSupport
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.RegistrationConfirmationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConfirmationController @Inject()(sessionRepository: SessionRepository,
                                                   identify: IdentifierActionProvider,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: RegistrationConfirmationFormProvider,
                                                   appConfig: AppConfig,
                                                   view: RegistrationConfirmationView)
                                                  (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify(false) andThen getData andThen requireData) { implicit request =>
    request.userAnswers.subscriptionDetails
      .map(_.subscriptionResponse)
      .filter(_.isInstanceOf[SubscribedResponse])
      .flatMap(_ => RegistrationConfirmationViewModel(mode, request.userAnswers, formProvider()))
      .map(viewModel => Ok(view(viewModel)(appConfig)))
      .getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify(false) andThen getData andThen requireData).async { implicit request =>
    formProvider().bindFromRequest().fold(
      formWithErrors => RegistrationConfirmationViewModel(mode, request.userAnswers, formWithErrors) match {
        case Some(viewModel) => Future.successful(BadRequest(view(viewModel)(appConfig)))
        case None => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      },
      value =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(RegistrationConfirmationPage, value))
          _ <- sessionRepository.set(updatedAnswers)
        } yield {
          val redirectUrl = if (value) appConfig.addPlatformOperatorUrl else appConfig.manageFrontendUrl
          Redirect(redirectUrl)
        }
    )
  }
}
