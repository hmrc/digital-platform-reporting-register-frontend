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
import forms.UtrFormProvider

import javax.inject.Inject
import models.Mode
import pages.UtrPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{UtrCorporationTaxView, UtrPartnershipView, UtrSelfAssessmentView}
import models.BusinessType
import models.BusinessType.*
import pages.BusinessTypePage

import scala.concurrent.{ExecutionContext, Future}

class UtrController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        sessionRepository: SessionRepository,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: UtrFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        corporationTaxView: UtrCorporationTaxView,
                                        partnershipView: UtrPartnershipView,
                                        selfAssessmentView: UtrSelfAssessmentView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with AnswerExtractor {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(UtrPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      getAnswer(BusinessTypePage) {
        businessType =>
          renderView(businessType, preparedForm, mode) match {
            case Some(view) => Ok(view)
            case _          => Redirect(routes.JourneyRecoveryController.onPageLoad())
          }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          getAnswerAsync(BusinessTypePage) {
            businessType => Future.successful(
              renderView(businessType, formWithErrors, mode) match {
                case Some(view) => BadRequest(view)
                case _ => Redirect(routes.JourneyRecoveryController.onPageLoad())
              }
            )
          },

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(UtrPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(UtrPage.nextPage(mode, updatedAnswers))
      )
  }

  private def renderView(businessType: BusinessType, form: Form[_], mode: Mode)(implicit request: Request[_]) =
    businessType match {
      case LimitedCompany | AssociationOrTrust => Some(corporationTaxView(form, mode))
      case Llp | Partnership                   => Some(partnershipView(form, mode))
      case SoleTrader                          => Some(selfAssessmentView(form, mode))
      case Individual                          => None
    }
}
