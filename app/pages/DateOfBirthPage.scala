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
import models.registration.responses.{AlreadySubscribedResponse, MatchResponseWithId, NoMatchResponse}
import models.{NormalMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import java.time.LocalDate

case object DateOfBirthPage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "dateOfBirth"

  override protected def nextPageNormalMode(answers: UserAnswers): Call = answers.registrationResponse match {
    case Some(value) =>
      value match {
      case _: AlreadySubscribedResponse => routes.IndividualAlreadyRegisteredController.onPageLoad()
      case _: MatchResponseWithId => routes.IndividualIdentityConfirmedController.onPageLoad()
      case _: NoMatchResponse => routes.IndividualIdentityNotConfirmedController.onPageLoad()
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }
    case None =>
      routes.AddressInUkController.onPageLoad(NormalMode)
  }
}
