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

package pages

import controllers.routes
import models.BusinessType.{Individual, SoleTrader}
import models.registration.responses as registrationResponses
import models.subscription.responses as subscriptionResponses
import models.{NormalMode, SubscriptionDetails, UserAnswers}
import play.api.mvc.Call

case object CheckYourAnswersPage extends Page {

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    answers.registrationResponse.map {
      case _: registrationResponses.NoMatchResponse =>
        routes.JourneyRecoveryController.onPageLoad()

      case _: registrationResponses.AlreadySubscribedResponse =>
        alreadySubscribedRoute(answers)

      case _ =>
        answers.subscriptionDetails.map {
          case SubscriptionDetails(subscriptionResponses.AlreadySubscribedResponse(), _, _, _, _) =>
            alreadySubscribedRoute(answers)

          case _ =>
            routes.RegistrationConfirmationController.onPageLoad(NormalMode)
        }.getOrElse(routes.JourneyRecoveryController.onPageLoad())
    }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private def alreadySubscribedRoute(answers: UserAnswers): Call =
    answers.subscriptionDetails match {
      case Some(details) =>
        details.businessType.map {
          case Individual | SoleTrader => routes.IndividualAlreadyRegisteredController.onPageLoad()
          case _ => routes.BusinessAlreadyRegisteredController.onPageLoad()
        }.getOrElse(routes.BusinessAlreadyRegisteredController.onPageLoad())
      case None => routes.JourneyRecoveryController.onPageLoad()
    }
}
