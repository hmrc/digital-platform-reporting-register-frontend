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

package navigation

import javax.inject.{Inject, Singleton}

import play.api.mvc.Call
import controllers.routes
import pages._
import models._
import models.BusinessType.SoleTrader

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case RegistrationTypePage => _ => routes.BusinessTypeController.onPageLoad(NormalMode)
    case UtrPage              => navigateFromUtrPage(_, NormalMode)
    case HasUtrPage           => hasUtrRoute
    case _                    => _ => routes.IndexController.onPageLoad()
  }

  private def hasUtrRoute(answers: UserAnswers): Call =
    answers.get(HasUtrPage).map {
      case true => routes.UtrController.onPageLoad(NormalMode)
      case false => routes.BusinessNameController.onPageLoad(NormalMode)
    }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }

  private def navigateFromUtrPage(userAnswers: UserAnswers, mode: Mode) =
    userAnswers.get(BusinessTypePage) match {
      case Some(SoleTrader) => routes.SoleTraderNameController.onPageLoad(mode)
      case Some(_)          => routes.BusinessNameController.onPageLoad(mode)
      case None             => routes.JourneyRecoveryController.onPageLoad()
    }
}
