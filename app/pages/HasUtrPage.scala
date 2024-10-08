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
import models.{Nino, NormalMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object HasUtrPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "hasUtr"

  override protected def nextPageNormalMode(answers: UserAnswers): Call = answers.get(this).map {
    case true => routes.UtrController.onPageLoad(NormalMode)
    case false => answers.get(BusinessTypePage).map {
      case Individual | SoleTrader => answers.user.taxIdentifier.map {
        case _: Nino => routes.IndividualNameController.onPageLoad(NormalMode)
        case _ => routes.HasNinoController.onPageLoad(NormalMode)
      }.getOrElse(routes.HasNinoController.onPageLoad(NormalMode))
      case _ => routes.BusinessNameNoUtrController.onPageLoad(NormalMode)
    }.getOrElse(routes.JourneyRecoveryController.onPageLoad())
  }.getOrElse(routes.JourneyRecoveryController.onPageLoad())
}
