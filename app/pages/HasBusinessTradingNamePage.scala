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
import models.{CheckMode, NormalMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.{Success, Try}

case object HasBusinessTradingNamePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "hasBusinessTradingName"

  override protected def nextPageNormalMode(answers: UserAnswers): Call = answers.get(this).map {
    case true => routes.BusinessEnterTradingNameController.onPageLoad(NormalMode)
    case false => routes.BusinessAddressController.onPageLoad(NormalMode)
  }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  override protected def nextPageCheckMode(answers: UserAnswers): Call = answers.get(this).map {
    case true => answers.get(BusinessEnterTradingNamePage)
      .map(_ => routes.CheckYourAnswersController.onPageLoad())
      .getOrElse(routes.BusinessEnterTradingNameController.onPageLoad(CheckMode))
    case false => routes.CheckYourAnswersController.onPageLoad()
  }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = value match {
    case Some(false) => userAnswers.remove(BusinessEnterTradingNamePage)
    case _ => Success(userAnswers)
  }
}
