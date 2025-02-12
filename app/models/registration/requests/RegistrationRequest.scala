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

import cats.data.{EitherNec, NonEmptyChain}
import models.BusinessType.{Individual, SoleTrader}
import models.{BusinessType, UserAnswers}
import pages.BusinessTypePage
import play.api.libs.json.{JsObject, Json, OFormat, OWrites}
import queries.Query

trait RegistrationRequest

object RegistrationRequest {

  implicit lazy val writes: OWrites[RegistrationRequest] = new OWrites[RegistrationRequest] {

    override def writes(o: RegistrationRequest): JsObject =
      o match {
        case x: IndividualWithNino => Json.toJsObject(x)(IndividualWithNino.writes)
        case x: IndividualWithUtr => Json.toJsObject(x)(IndividualWithUtr.writes)
        case x: OrganisationWithUtr => Json.toJsObject(x)(OrganisationWithUtr.writes)
        case x: IndividualWithoutId => Json.toJsObject(x)(IndividualWithoutId.writes)
        case x: OrganisationWithoutId => Json.toJsObject(x)(OrganisationWithoutId.writes)
      }
  }

  def build(userAnswers: UserAnswers): EitherNec[Query, RegistrationRequest] = userAnswers.getEither(BusinessTypePage).flatMap {
    case SoleTrader | Individual => IndividualWithoutId.build(userAnswers)
    case _ => OrganisationWithoutId.build(userAnswers)
  }

  final case class BuildRegistrationRequestFailure(errors: NonEmptyChain[Query]) extends Throwable {
    override def getMessage: String = s"Unable to build a registration request, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}"
  }
}

case class ContactDetails(emailAddress: String, phoneNumber: Option[String])

object ContactDetails {
  implicit val format: OFormat[ContactDetails] = Json.format[ContactDetails]
}