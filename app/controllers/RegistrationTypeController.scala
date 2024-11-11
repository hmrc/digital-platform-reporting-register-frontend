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

import forms.RegistrationTypeFormProvider
import models.{Mode, RegistrationType}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.RegistrationTypeView

import javax.inject.Inject

class RegistrationTypeController @Inject()(formProvider: RegistrationTypeFormProvider,
                                           view: RegistrationTypeView)
                                          (implicit mcc: MessagesControllerComponents)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = Action { implicit request =>
    Ok(view(formProvider(), mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = Action { implicit request =>
    formProvider().bindFromRequest().fold(
      formWithErrors => BadRequest(view(formWithErrors, mode)),
      {
        case RegistrationType.PlatformOperator => Redirect(routes.RegistrationTypeContinueController.platformOperator())
        case RegistrationType.ThirdParty => Redirect(routes.RegistrationTypeContinueController.thirdParty())
      }
    )
  }

}
