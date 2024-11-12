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
import models.pageviews.RegistrationConfirmationViewModel
import models.subscription.responses.SubscribedResponse
import play.api.i18n.I18nSupport
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.RegistrationConfirmationView

import javax.inject.Inject

class RegistrationConfirmationController @Inject()(identify: IdentifierActionProvider,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   appConfig: AppConfig,
                                                   view: RegistrationConfirmationView)
                                                  (implicit mcc: MessagesControllerComponents)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify(false) andThen getData andThen requireData) { implicit request =>
    request.userAnswers.subscriptionDetails
      .map(_.subscriptionResponse)
      .filter(_.isInstanceOf[SubscribedResponse])
      .flatMap(_ => RegistrationConfirmationViewModel(request.userAnswers))
      .map(viewModel => Ok(view(viewModel)(appConfig)))
      .getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
  }
}
