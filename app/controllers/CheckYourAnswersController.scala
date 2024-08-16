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

import cats.data.NonEmptyChain
import com.google.inject.Inject
import connectors.{RegistrationConnector, SubscriptionConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.BusinessType.*
import models.pageviews.{CheckYourAnswersIndividualViewModel, CheckYourAnswersOrganisationViewModel}
import models.registration.requests.OrganisationWithoutId
import models.registration.responses.{MatchResponseWithId, MatchResponseWithoutId, RegistrationResponse}
import models.requests.DataRequest
import models.subscription.requests.SubscriptionRequest
import models.{NormalMode, UserAnswers}
import pages.*
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.Query
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{CheckYourAnswersIndividualView, CheckYourAnswersOrganisationView}

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           individualView: CheckYourAnswersIndividualView,
                                           organisationView: CheckYourAnswersOrganisationView,
                                           registrationConnector: RegistrationConnector,
                                           subscriptionConnector: SubscriptionConnector,
                                           sessionRepository: SessionRepository)
                                          (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with AnswerExtractor with Logging {

  private lazy val failedSubRequestBuildError =
    (errors: NonEmptyChain[Query]) => {
      logger.error(
        s"Unable to build subscription request, path(s) missing:${
          errors.toChain.toList.map(_.path)
            .mkString(", ")
        }"
      )
      Option.empty[SubscriptionRequest]
    }

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(BusinessTypePage).map {
      case SoleTrader | Individual => showIndividual(implicitly)
      case _ => showOrganisation(implicitly)
    }.getOrElse(showOrganisation(implicitly))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    val userAnswers = request.userAnswers

    val result: Future[Result] =
      userAnswers.get(BusinessTypePage).flatMap {
        case SoleTrader | Individual =>
          Some(
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())) //TODO ODPR-1283/SUB-15
          )
      case _ => Some(submitForOrganisation)
    }.getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
    result
  }
  
  private def submitForOrganisation(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val userAnswers = request.userAnswers
    for {
      regResponse <- getRegistrationResponse(userAnswers)
      updatedAnswers = request.userAnswers.copy(registrationResponse = Some(regResponse))
      _ <- sessionRepository.set(updatedAnswers)
      maybeSubscriptionRequest = {
        regResponse match {
          case MatchResponseWithoutId(safeId) =>
            SubscriptionRequest.build(safeId, userAnswers, None)
              .fold(failedSubRequestBuildError, Some(_))
          case MatchResponseWithId(safeId, address, _) =>
            SubscriptionRequest.build(safeId, userAnswers, Some(address))
              .fold(failedSubRequestBuildError, Some(_))
          case _ => Option.empty[SubscriptionRequest]
        }
      }
      result <- maybeSubscriptionRequest match {
        case Some(subRequest) =>
          for {
            response <- subscriptionConnector.subscribe(subRequest)
            updatedAnswers = request.userAnswers.copy(subscriptionResponse = Some(response))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(CheckYourAnswersPage.nextPage(NormalMode, updatedAnswers))
        case None =>
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    } yield result
  }
  
  private def getRegistrationResponse(userAnswers: UserAnswers)(implicit request: DataRequest[AnyContent]) = {
    if (userAnswers.registrationResponse.isDefined) {
      Future.successful(userAnswers.registrationResponse.get)
    } else {
      val futureRequestBody = OrganisationWithoutId.build(userAnswers).fold(
        errors =>
          Future.failed[OrganisationWithoutId](Exception(
            s"Unable to build registration request, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}"
          )),
        Future.successful
      )

      futureRequestBody.flatMap { requestBody =>
        registrationConnector.register(requestBody)
      }
    }
  }

  private def showIndividual(implicit request: DataRequest[AnyContent]) = {
    val viewModel = CheckYourAnswersIndividualViewModel.apply(request.userAnswers)
    Ok(individualView(viewModel))
  }

  private def showOrganisation(implicit request: DataRequest[AnyContent]) = {
    getModel
      .map(viewModel => Ok(organisationView(viewModel)))
      .getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
  }

  private def getModel(implicit request: DataRequest[AnyContent]) = {
    CheckYourAnswersOrganisationViewModel
      .apply(request.userAnswers)
  }
}
