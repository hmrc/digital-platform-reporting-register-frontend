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

import scala.util.Try

case object AddressInUkPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "addressInUk"

  override protected def nextPageNormalMode(answers: UserAnswers): Call = answers.get(this).map {
    case true => routes.UkAddressController.onPageLoad(NormalMode)
    case false => routes.InternationalAddressController.onPageLoad(NormalMode)
  }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  override protected def nextPageCheckMode(answers: UserAnswers): Call = answers.get(this).map {
    case true =>
      if (answers.isDefined(UkAddressPage)) {
        routes.CheckYourAnswersController.onPageLoad()
      } else {
        routes.UkAddressController.onPageLoad(CheckMode)
      }
    case false =>
      if (answers.isDefined(InternationalAddressPage)) {
        routes.CheckYourAnswersController.onPageLoad()
      } else {
        routes.InternationalAddressController.onPageLoad(CheckMode)
      }
  }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(true)) {
      userAnswers.remove(InternationalAddressPage)
    } else {
      userAnswers.remove(UkAddressPage)
    }
}
