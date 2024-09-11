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
import models.registration.Address
import models.{BusinessAddress, UserAnswers}
import pages.*
import play.api.libs.json.{Json, OWrites}
import queries.Query

import scala.util.Right

final case class OrganisationWithoutId(name: String,
                                       address: Address,
                                       contactDetails: ContactDetails) extends RegistrationRequest

object OrganisationWithoutId {

  implicit lazy val writes: OWrites[OrganisationWithoutId] = Json.writes

  def build(answers: UserAnswers): EitherNec[Query, OrganisationWithoutId] =
    (
      answers.getEither(BusinessNameNoUtrPage),
      answers.getEither(BusinessAddressPage),
      answers.getEither(PrimaryContactNamePage),
      answers.getEither(PrimaryContactEmailAddressPage),
      getPhoneNumber(answers)
    ).parMapN { (name, businessAddress, contactName, email, phone) =>
      OrganisationWithoutId(name, Address(businessAddress), ContactDetails(email, phone))
    }

  private def getPhoneNumber(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(CanPhonePrimaryContactPage).flatMap {
      case true => answers.getEither(PrimaryContactPhoneNumberPage).map(Some(_))
      case false => Right(None)
    }
}
