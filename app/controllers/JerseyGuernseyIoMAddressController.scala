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
import forms.JerseyGuernseyIoMAddressFormProvider
import models.{CountriesList, JerseyGuernseyIoMAddress, Mode, UkAddress}
import pages.{JerseyGuernseyIoMAddressPage, UkAddressPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.JerseyGuernseyIoMAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JerseyGuernseyIoMAddressController @Inject()(sessionRepository: SessionRepository,
                                                   identify: IdentifierActionProvider,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: JerseyGuernseyIoMAddressFormProvider,
                                                   view: JerseyGuernseyIoMAddressView,
                                                   val countriesList: CountriesList)
                                                  (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData) { implicit request =>

    val form = formProvider()
    val preparedForm = request.userAnswers.get(JerseyGuernseyIoMAddressPage) match {
      case None => request.userAnswers.get(UkAddressPage) match {
        case Some(value) if countriesList.crownDependantCountries.exists(_.code == value.country.code) => form.fill(convertToJerseyGuernseyIoMAddress(value))
        case _ => form
      }

      case Some(value) => formProvider().fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData).async { implicit request =>
    formProvider().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
      value => for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(JerseyGuernseyIoMAddressPage, value))
        _ <- sessionRepository.set(updatedAnswers)
      } yield Redirect(JerseyGuernseyIoMAddressPage.nextPage(mode, updatedAnswers))
    )
  }

  private def convertToJerseyGuernseyIoMAddress(ukAddress: UkAddress): JerseyGuernseyIoMAddress = {
    JerseyGuernseyIoMAddress(ukAddress.line1,
      ukAddress.line2,
      ukAddress.town,
      ukAddress.county,
      ukAddress.postCode,
      ukAddress.country)
  }
}
