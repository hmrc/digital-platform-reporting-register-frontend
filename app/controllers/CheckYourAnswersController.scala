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
import connectors.SubscriptionConnector.SubscribeFailure
import connectors.{RegistrationConnector, SubscriptionConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierActionProvider}
import models.BusinessType.*
import models.audit.AuditEventModel
import models.eacd.{EnrolmentDetails, EnrolmentKnownFacts}
import models.pageviews.{CheckYourAnswersIndividualViewModel, CheckYourAnswersOrganisationViewModel}
import models.registration.requests.{IndividualWithoutId, OrganisationWithoutId}
import models.registration.responses as registrationResponses
import models.registration.responses.RegistrationResponse
import models.requests.UserSessionDataRequest
import models.subscription.requests.SubscriptionRequest
import models.subscription.responses.SubscribedResponse
import models.{NormalMode, SubscriptionDetails, UserAnswers}
import pages.{BusinessTypePage, CheckYourAnswersPage}
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import repositories.SessionRepository
import services.{AuditService, EnrolmentService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{CheckYourAnswersIndividualView, CheckYourAnswersOrganisationView}

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(identify: IdentifierActionProvider,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           individualView: CheckYourAnswersIndividualView,
                                           organisationView: CheckYourAnswersOrganisationView,
                                           registrationConnector: RegistrationConnector,
                                           subscriptionConnector: SubscriptionConnector,
                                           enrolmentService: EnrolmentService,
                                           auditService: AuditService,
                                           sessionRepository: SessionRepository)
                                          (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with AnswerExtractor {

  def onPageLoad(): Action[AnyContent] = (identify() andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(BusinessTypePage).map {
      case SoleTrader | Individual => showIndividual(implicitly)
      case _ => showOrganisation(implicitly)
    }.getOrElse(showOrganisation(implicitly))
  }

  def onSubmit(): Action[AnyContent] = (identify() andThen getData andThen requireData).async { implicit request =>
    getRegistrationResponse(request.userAnswers).flatMap {
      case response: registrationResponses.AlreadySubscribedResponse =>
        val answersWithRegistration = request.userAnswers.copy(registrationResponse = Some(response))
        Future.successful(Redirect(CheckYourAnswersPage.nextPage(NormalMode, answersWithRegistration)))

      case _: registrationResponses.NoMatchResponse =>
        Future.failed(new Exception("Registration response is No Match"))

      case matchResponse: registrationResponses.MatchResponse =>
        EnrolmentKnownFacts(request.userAnswers) match {
          case None => Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          case Some(enrolmentKnownFacts) =>
            val answersWithRegistration = request.userAnswers.copy(registrationResponse = Some(matchResponse))

            subscribe(matchResponse.safeId, answersWithRegistration, request.userAnswers).flatMap { subscriptionDetails =>
              val answersWithSubscription = answersWithRegistration.copy(
                subscriptionDetails = Some(subscriptionDetails),
                data = Json.obj()
              )
              subscriptionDetails.subscriptionResponse match {
                case subscribedResponse: SubscribedResponse =>
                  enrolmentService.enrol(EnrolmentDetails(enrolmentKnownFacts, subscribedResponse.dprsId)).flatMap { _ =>
                    sessionRepository.set(answersWithSubscription).map { _ =>
                      Redirect(CheckYourAnswersPage.nextPage(NormalMode, answersWithSubscription))
                    }
                  }
                case _ => sessionRepository.set(answersWithSubscription).map { _ =>
                  Redirect(CheckYourAnswersPage.nextPage(NormalMode, answersWithSubscription))
                }
              }
            }
        }
    }
  }

  private def getRegistrationResponse(answers: UserAnswers)(implicit request: Request[_]): Future[RegistrationResponse] =
    answers.registrationResponse
      .map(x => Future.successful(x))
      .getOrElse {
        answers.get(BusinessTypePage).map {
          case Individual | SoleTrader => IndividualWithoutId.build(answers).fold(
            errors => Future.failed(Exception(s"Unable to build a registration request for an individual, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}")),
            request => registrationConnector.register(request)
          )
          case _ => OrganisationWithoutId.build(answers).fold(
            errors => Future.failed(Exception(s"Unable to build a registration request for an organisation, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}")),
            request => registrationConnector.register(request)
          )
        }.getOrElse(Future.failed(Exception("Could not find an answer for BusinessType when trying to build a registration request")))
      }

  private def subscribe(safeId: String, answersWithRegistration: UserAnswers, originalAnswers: UserAnswers)
                       (implicit request: Request[_]): Future[SubscriptionDetails] =
    lazy val isAutoSubscription = originalAnswers.registrationResponse.isEmpty
    SubscriptionRequest.build(safeId, answersWithRegistration).fold(
      errors => Future.failed(Exception(s"Unable to build a subscription request, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}")),
      request =>
        subscriptionConnector
          .subscribe(request)
          .map { response =>
            auditService.sendAudit(AuditEventModel(isAutoSubscription, answersWithRegistration.data, response))
            SubscriptionDetails(response, request, answersWithRegistration)
          }
          .recover {
            case error =>
              auditService.sendAudit(AuditEventModel(isAutoSubscription, answersWithRegistration.data, error.asInstanceOf[SubscribeFailure]))
              throw error
          }
    )

  private def showIndividual(implicit request: UserSessionDataRequest[AnyContent]) = {
    val viewModel = CheckYourAnswersIndividualViewModel(request.userAnswers)
    Ok(individualView(viewModel))
  }

  private def showOrganisation(implicit request: UserSessionDataRequest[AnyContent]) =
    CheckYourAnswersOrganisationViewModel(request.userAnswers)
      .map(viewModel => Ok(organisationView(viewModel)))
      .getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
}
