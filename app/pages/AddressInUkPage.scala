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
import models.registration.RegisteredAddressCountry
import models.registration.RegisteredAddressCountry.{International, JerseyGuernseyIsleOfMan, Uk}
import models.{CheckMode, NormalMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object AddressInUkPage extends QuestionPage[RegisteredAddressCountry] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "addressInUk"

  override protected def nextPageNormalMode(answers: UserAnswers): Call = answers.get(this).map {
    case Uk => routes.UkAddressController.onPageLoad(NormalMode)
    case JerseyGuernseyIsleOfMan => routes.JerseyGuernseyIoMAddressController.onPageLoad(NormalMode)
    case International => routes.InternationalAddressController.onPageLoad(NormalMode)
  }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  override protected def nextPageCheckMode(answers: UserAnswers): Call = answers.get(this).map {
    case Uk => answers.get(UkAddressPage)
      .map(_ => routes.CheckYourAnswersController.onPageLoad())
      .getOrElse(routes.UkAddressController.onPageLoad(CheckMode))

    case JerseyGuernseyIsleOfMan => answers.get(JerseyGuernseyIoMAddressPage)
      .map(_ => routes.CheckYourAnswersController.onPageLoad())
      .getOrElse(routes.JerseyGuernseyIoMAddressController.onPageLoad(CheckMode))

    case International => answers.get(InternationalAddressPage)
      .map(_ => routes.CheckYourAnswersController.onPageLoad())
      .getOrElse(routes.InternationalAddressController.onPageLoad(CheckMode))
  }.getOrElse(routes.JourneyRecoveryController.onPageLoad())

  override def cleanup(value: Option[RegisteredAddressCountry], userAnswers: UserAnswers): Try[UserAnswers] = {
    value.map {
      case Uk => userAnswers.remove(InternationalAddressPage).flatMap(_.remove(JerseyGuernseyIoMAddressPage))
      case International => userAnswers.remove(UkAddressPage).flatMap(_.remove(JerseyGuernseyIoMAddressPage))
      case JerseyGuernseyIsleOfMan => userAnswers.remove(InternationalAddressPage).flatMap(_.remove(UkAddressPage))
    }.getOrElse(super.cleanup(value, userAnswers))
  }
}
