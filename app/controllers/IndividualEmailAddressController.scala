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

import controllers.actions.*
import forms.IndividualEmailAddressFormProvider
import models.Mode
import models.pageviews.IndividualEmailAddressViewModel
import pages.IndividualEmailAddressPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.IndividualEmailAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndividualEmailAddressController @Inject()(sessionRepository: SessionRepository,
                                                 identify: IdentifierActionProvider,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: IndividualEmailAddressFormProvider,
                                                 view: IndividualEmailAddressView)
                                                (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData) { implicit request =>
    Ok(view(IndividualEmailAddressViewModel(mode, request.userAnswers, formProvider())))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData).async { implicit request =>
    formProvider().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(IndividualEmailAddressViewModel(mode, request.userAnswers, formWithErrors)))),
      value =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(IndividualEmailAddressPage, value))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(IndividualEmailAddressPage.nextPage(mode, updatedAnswers))
    )
  }
}
