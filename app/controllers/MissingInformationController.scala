/*
 * Copyright 2025 HM Revenue & Customs
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

import controllers.actions.*
import models.NormalMode
import models.registration.requests.RegistrationRequest
import models.registration.responses.MatchResponse
import models.subscription.requests.SubscriptionRequest
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.MissingInformationView

import javax.inject.Inject
import scala.concurrent.Future

class MissingInformationController @Inject()(identify: IdentifierActionProvider,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             view: MissingInformationView)
                                            (using mmc: MessagesControllerComponents)
  extends FrontendController(mmc) with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify() andThen getData andThen requireData) { implicit request =>
    Ok(view())
  }

  def onSubmit(): Action[AnyContent] = (identify() andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.registrationResponse match {
      case None => RegistrationRequest.build(request.userAnswers).fold(
        _ => Future.successful(Redirect(routes.BusinessTypeController.onPageLoad(NormalMode).url)),
        _ => Future.successful(Redirect(routes.CheckYourAnswersController.onPageLoad()))
      )
      case Some(matchResponse: MatchResponse) => SubscriptionRequest.build(matchResponse.safeId, request.userAnswers).fold(
        _ => Future.successful(Redirect(routes.PrimaryContactNameController.onPageLoad(NormalMode))),
        _ => Future.successful(Redirect(routes.CheckYourAnswersController.onPageLoad()))
      )
      case _ => Future.successful(Redirect(routes.CheckYourAnswersController.onPageLoad()))
    }
  }
}
