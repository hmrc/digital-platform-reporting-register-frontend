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
import forms.PrimaryContactEmailAddressFormProvider
import models.Mode
import models.pageviews.PrimaryContactEmailAddressViewModel
import pages.PrimaryContactEmailAddressPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.PrimaryContactEmailAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PrimaryContactEmailAddressController @Inject()(sessionRepository: SessionRepository,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: PrimaryContactEmailAddressFormProvider,
                                      view: PrimaryContactEmailAddressView)
                                     (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userAnswers = request.userAnswers
    Ok(view(PrimaryContactEmailAddressViewModel(mode, userAnswers, formProvider())))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    formProvider().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(PrimaryContactEmailAddressViewModel(mode, request.userAnswers, formWithErrors)))),
      value =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(PrimaryContactEmailAddressPage, value))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(PrimaryContactEmailAddressPage.nextPage(mode, updatedAnswers))
    )
  }
}
