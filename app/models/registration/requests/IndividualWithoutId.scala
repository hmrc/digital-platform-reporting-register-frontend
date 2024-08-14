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
import models.registration.Address
import pages.*
import play.api.libs.json.{JsObject, Json, OWrites}
import queries.Query

import java.time.LocalDate


final case class IndividualWithoutId(firstName: String,
                                     lastName: String,
                                     dateOfBirth: LocalDate,
                                     address: Address) extends RegistrationRequest

object IndividualWithoutId {

  implicit lazy val writes: OWrites[IndividualWithoutId] = Json.writes

  def build(answers: UserAnswers): EitherNec[Query, IndividualWithoutId] = answers.get(AddressInUkPage) match {
    case Some(true) => (
      answers.getEither(IndividualNamePage).map(_.firstName),
      answers.getEither(IndividualNamePage).map(_.lastName),
      answers.getEither(DateOfBirthPage),
      answers.getEither(UkAddressPage).map(Address(_)),
    ).parMapN(IndividualWithoutId.apply)
    case _ => (
      answers.getEither(IndividualNamePage).map(_.firstName),
      answers.getEither(IndividualNamePage).map(_.lastName),
      answers.getEither(DateOfBirthPage),
      answers.getEither(InternationalAddressPage).map(Address(_)),
    ).parMapN(IndividualWithoutId.apply)
  }
}
