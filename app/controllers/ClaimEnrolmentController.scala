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

import config.{AppConfig, SubscriptionIdentifiersProvider}
import connectors.RegistrationConnector
import controllers.actions.*
import forms.ClaimEnrolmentFormProvider
import models.eacd.{EnrolmentDetails, Identifier}
import models.registration.requests.{OrganisationDetails, OrganisationWithUtr}
import models.registration.responses.MatchResponseWithId
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.EnrolmentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.ClaimEnrolmentView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimEnrolmentController @Inject()(identify: IdentifierActionProvider,
                                         formProvider: ClaimEnrolmentFormProvider,
                                         view: ClaimEnrolmentView,
                                         subscriptionIdentifiersProvider: SubscriptionIdentifiersProvider,
                                         registrationConnector: RegistrationConnector,
                                         enrolmentService: EnrolmentService,
                                         appConfig: AppConfig
                                        )(implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify() { implicit request =>
    Ok(view(formProvider()))
  }

  def onSubmit(): Action[AnyContent] = identify().async { implicit request =>
    subscriptionIdentifiersProvider.subscriptionIdentifiers.map { subscriptionIdentifiers =>
      formProvider().bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value => {
          val registrationRequest = OrganisationWithUtr(
            utr     = value.utr,
            details = Some(OrganisationDetails(value.businessName, value.businessType))
          )

          registrationConnector.register(registrationRequest).flatMap {
            case matchResponse: MatchResponseWithId =>
              if (matchResponse.safeId == subscriptionIdentifiers.safeId) {
                val enrolmentDetails = for {
                  providerId <- request.user.providerId
                  groupId <- request.user.groupId
                } yield EnrolmentDetails(providerId, "UTR", value.utr, groupId, Identifier("DPRSID", subscriptionIdentifiers.dprsId))
                
                enrolmentDetails.map { details =>
                  enrolmentService.enrol(details).map(_ => Redirect(appConfig.manageFrontendUrl))
                }.getOrElse(redirectOnFail)
              } else {
                redirectOnFail
              }
              
            case _ => redirectOnFail
          }
        }
      )
    }.getOrElse(redirectOnFail)
  }
  
  private lazy val redirectOnFail: Future[Result] =
    Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
}
