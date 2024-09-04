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
import play.api.libs.json.{Json, OWrites}
import queries.Query

import java.time.LocalDate

final case class IndividualWithoutId(firstName: String,
                                     lastName: String,
                                     dateOfBirth: LocalDate,
                                     address: Address,
                                     contactDetails: ContactDetails
                                    ) extends RegistrationRequest

object IndividualWithoutId {

  implicit lazy val writes: OWrites[IndividualWithoutId] = Json.writes

  def build(answers: UserAnswers): EitherNec[Query, IndividualWithoutId] =
    (
      answers.getEither(IndividualNamePage),
      answers.getEither(DateOfBirthPage),
      getAddress(answers),
      answers.getEither(IndividualEmailAddressPage),
      getPhoneNumber(answers)
    ).parMapN { (name, dateOfBirth, address, email, phone) =>
      IndividualWithoutId(
        name.firstName,
        name.lastName,
        dateOfBirth,
        address,
        ContactDetails(email, phone)
      )
    }

  private def getAddress(answers: UserAnswers): EitherNec[Query, Address] =
    answers.getEither(AddressInUkPage).flatMap {
      case true => answers.getEither(UkAddressPage).map(Address.fromUkAddress)
      case false => answers.getEither(InternationalAddressPage).map(Address.fromInternationalAddress)
    }

  private def getPhoneNumber(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(CanPhoneIndividualPage).flatMap {
      case true => answers.getEither(IndividualPhoneNumberPage).map(Some(_))
      case false => Right(None)
    }
}
