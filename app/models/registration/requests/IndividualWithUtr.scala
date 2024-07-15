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

package models.registration.requests

import cats.data.*
import cats.implicits.*
import models.UserAnswers
import pages.{SoleTraderNamePage, UtrPage}
import play.api.libs.json.*
import queries.Query

final case class IndividualWithUtr(utr: String, details: IndividualDetails) extends RegistrationRequest

object IndividualWithUtr {
  
  def build(answers: UserAnswers): EitherNec[Query, IndividualWithUtr] =
    (
      answers.getEither(UtrPage),
      answers.getEither(SoleTraderNamePage)
        .map(name => IndividualDetails(name.firstName, name.lastName))
    ).parMapN(IndividualWithUtr.apply)
  
  implicit lazy val writes: OWrites[IndividualWithUtr] = new OWrites[IndividualWithUtr] {
    override def writes(o: IndividualWithUtr): JsObject =
      Json.obj(
        "type" -> "individual",
        "utr" -> o.utr,
        "details" -> Json.toJson(o.details)
      )
  }
}
