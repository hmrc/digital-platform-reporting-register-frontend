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
import forms.IsThisYourBusinessFormProvider
import models.Mode
import models.registration.Address
import models.registration.responses.MatchResponseWithId
import models.requests.UserSessionDataRequest
import pages.IsThisYourBusinessPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.IsThisYourBusinessView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsThisYourBusinessController @Inject()(sessionRepository: SessionRepository,
                                             identify: IdentifierActionProvider,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             formProvider: IsThisYourBusinessFormProvider,
                                             view: IsThisYourBusinessView)
                                            (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers.get(IsThisYourBusinessPage) match {
      case None => formProvider()
      case Some(value) => formProvider().fill(value)
    }

    showPage((businessName, address) => Ok(view(preparedForm, businessName, address, mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify() andThen getData andThen requireData).async { implicit request =>
    formProvider().bindFromRequest().fold(
      formWithErrors => Future.successful(showPage((businessName, address) => BadRequest(view(formWithErrors, businessName, address, mode)))),
      value =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(IsThisYourBusinessPage, value))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(IsThisYourBusinessPage.nextPage(mode, updatedAnswers))
    )
  }

  private def showPage(page: (String, Address) => Result)(implicit request: UserSessionDataRequest[AnyContent]): Result = {
    request.userAnswers.registrationResponse match {
      case Some(r) => r match {
        case r: MatchResponseWithId => r.organisationName match {
          case Some(name) => page(name, r.address)
          case _ => error
        }
        case _ => error
      }
      case _ => error
    }
  }

  private def error =
    Redirect(routes.JourneyRecoveryController.onPageLoad())
}
