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

import com.google.inject.Inject
import connectors.{RegistrationConnector, SubscriptionConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.BusinessType.*
import models.{NormalMode, RegistrationType, SubscriptionDetails, UserAnswers}
import models.pageviews.{CheckYourAnswersIndividualViewModel, CheckYourAnswersOrganisationViewModel}
import models.registration.requests.{IndividualWithoutId, OrganisationWithoutId}
import models.registration.responses as registrationResponses
import models.registration.responses.RegistrationResponse
import models.requests.DataRequest
import models.subscription.requests.SubscriptionRequest
import models.subscription.responses as subscriptionResponses
import models.subscription.responses.SubscriptionResponse
import pages.{BusinessTypePage, CheckYourAnswersPage, RegistrationTypePage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
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
  extends FrontendController(mcc) with I18nSupport with AnswerExtractor {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(BusinessTypePage).map {
      case SoleTrader | Individual => showIndividual(implicitly)
      case _ => showOrganisation(implicitly)
    }.getOrElse(showOrganisation(implicitly))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    getRegistrationResponse(request.userAnswers).flatMap {
      case response: registrationResponses.AlreadySubscribedResponse =>
        val answersWithRegistration = request.userAnswers.copy(registrationResponse = Some(response))
        Future.successful(Redirect(CheckYourAnswersPage.nextPage(NormalMode, answersWithRegistration)))

      case _: registrationResponses.NoMatchResponse =>
        Future.failed(new Exception("Registration response is No Match"))

      case matchResponse: registrationResponses.MatchResponse =>
        val answersWithRegistration = request.userAnswers.copy(registrationResponse = Some(matchResponse))

        subscribe(matchResponse.safeId, answersWithRegistration).flatMap { subscriptionDetails =>
          val answersWithSubscription =
            answersWithRegistration.copy(
              subscriptionDetails = Some(subscriptionDetails),
              data = Json.obj()
            )

          sessionRepository.set(answersWithSubscription).map { _ =>
            Redirect(CheckYourAnswersPage.nextPage(NormalMode, answersWithSubscription))
          }
        }
    }
  }

  private def getRegistrationResponse(answers: UserAnswers)(implicit request: Request[_]): Future[RegistrationResponse] =
    answers.registrationResponse
      .map(x => Future.successful(x))
      .getOrElse {
        answers.get(BusinessTypePage).map {
          case Individual | SoleTrader =>
            IndividualWithoutId.build(answers)
              .fold(
                errors => Future.failed(Exception(s"Unable to build a registration request for an individual, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}")),
                request => registrationConnector.register(request)
              )
          case _ =>
            OrganisationWithoutId.build(answers)
              .fold(
                errors => Future.failed(Exception(s"Unable to build a registration request for an organisation, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}")),
                request => registrationConnector.register(request)
              )
        }.getOrElse(Future.failed(Exception("Could not find an answer for BusinessType when trying to build a registration request")))
      }

  private def subscribe(safeId: String, answers: UserAnswers)(implicit request: Request[_]): Future[SubscriptionDetails] =
    SubscriptionRequest.build(safeId, answers)
      .fold(
        errors => Future.failed(Exception(s"Unable to build a subscription request, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}")),
        request =>
          subscriptionConnector
            .subscribe(request)
            .map { response =>
              val registrationType = answers.get(RegistrationTypePage).getOrElse(RegistrationType.ThirdParty)
              SubscriptionDetails(response, request, registrationType)
            }
      )

  private def showIndividual(implicit request: DataRequest[AnyContent]) = {
    val viewModel = CheckYourAnswersIndividualViewModel(request.userAnswers)
    Ok(individualView(viewModel))
  }

  private def showOrganisation(implicit request: DataRequest[AnyContent]) =
    CheckYourAnswersOrganisationViewModel(request.userAnswers)
      .map(viewModel => Ok(organisationView(viewModel)))
      .getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
}
