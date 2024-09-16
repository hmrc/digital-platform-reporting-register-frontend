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
import forms.BusinessAddressFormProvider
import models.Mode
import models.pageviews.BusinessAddressViewModel
import pages.BusinessAddressPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.BusinessAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessAddressController @Inject()(sessionRepository: SessionRepository,
                                          identify: IdentifierActionProvider,
                                          getData: DataRetrievalAction,
                                          requireData: DataRequiredAction,
                                          formProvider: BusinessAddressFormProvider,
                                          view: BusinessAddressView)
                                         (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData) { implicit request =>
    val userAnswers = request.userAnswers
    Ok(view(BusinessAddressViewModel(mode, userAnswers, formProvider())))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData).async { implicit request =>
    formProvider().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(BusinessAddressViewModel(mode, request.userAnswers, formWithErrors)))),
      value =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessAddressPage, value))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(BusinessAddressPage.nextPage(mode, updatedAnswers))
    )
  }
}
