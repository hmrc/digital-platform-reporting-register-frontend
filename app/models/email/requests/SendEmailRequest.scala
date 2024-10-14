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

package models.email.requests

import cats.data.*
import cats.implicits.*
import models.BusinessType.SoleTrader
import models.{BusinessType, IndividualName, SoleTraderName, UserAnswers, Utr}
import pages.*
import play.api.libs.json.*
import queries.Query

case class SendEmailRequest(to: List[String],
                            templateId: String,
                            parameters: Map[String, String])

object SendEmailRequest {
  implicit val format: OFormat[SendEmailRequest] = Json.format[SendEmailRequest]

  private val RegistrationSubmittedTemplateId: String = "dprs_registration_submitted"

  def apply(email: String, dprsId: String, name: String): SendEmailRequest = SendEmailRequest(
    to = List(email),
    templateId = RegistrationSubmittedTemplateId,
    parameters = Map("dprsId" -> dprsId, "name" -> name)
  )

  def build(answers: UserAnswers, dprsId: String): EitherNec[Query, SendEmailRequest] =
    (
      getPrimaryContactEmail(answers),
      getPrimaryContactName(answers)
    ).parMapN(SendEmailRequest(_, dprsId, _))

  private[requests] def getPrimaryContactEmail(answers: UserAnswers): EitherNec[Query, String] = answers.user.taxIdentifier match {
    case Some(Utr(_)) => answers.getEither(PrimaryContactEmailAddressPage)
    case _ => answers.getEither(BusinessTypePage).flatMap {
      case BusinessType.Individual | SoleTrader => answers.getEither(IndividualEmailAddressPage)
      case _ => answers.getEither(PrimaryContactEmailAddressPage)
    }
  }

  private[requests] def getPrimaryContactName(answers: UserAnswers): EitherNec[Query, String] = answers.user.taxIdentifier match {
    case Some(Utr(_)) => answers.getEither(PrimaryContactNamePage)
    case _ => answers.getEither(BusinessTypePage).flatMap {
      case BusinessType.Individual | SoleTrader => getIndividualContactName(answers)
      case _ => answers.getEither(PrimaryContactNamePage)
    }
  }

  private[requests] def getIndividualContactName(answers: UserAnswers): EitherNec[Query, String] =
    answers.get(IndividualNamePage)
      .map(item => Right(s"${item.firstName} ${item.lastName}"))
      .getOrElse(answers.getEither(SoleTraderNamePage).map(item => s"${item.firstName} ${item.lastName}"))
}
