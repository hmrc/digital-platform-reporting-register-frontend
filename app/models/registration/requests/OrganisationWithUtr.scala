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
import models.{UserAnswers, Utr}
import pages.{BusinessNamePage, BusinessTypePage, UtrPage}
import play.api.libs.json.{JsObject, Json, OWrites}
import queries.Query

final case class OrganisationWithUtr(utr: String, details: Option[OrganisationDetails]) extends RegistrationRequest

object OrganisationWithUtr {

  def build(answers: UserAnswers): EitherNec[Query, OrganisationWithUtr] =
    answers.user.taxIdentifier.map {
      case Utr(utr) => Right(OrganisationWithUtr(utr, None))
      case _ => buildWithDetails(answers)
    }.getOrElse(buildWithDetails(answers))

  private def buildWithDetails(answers: UserAnswers): EitherNec[Query, OrganisationWithUtr] =
    (
      answers.getEither(UtrPage),
      answers.getEither(BusinessTypePage),
      answers.getEither(BusinessNamePage)
    ).parMapN { (utr, businessType, name) =>
      OrganisationWithUtr(utr, Some(OrganisationDetails(name, businessType)))
    }

  implicit lazy val writes: OWrites[OrganisationWithUtr] = new OWrites[OrganisationWithUtr] {
    override def writes(o: OrganisationWithUtr): JsObject = {

      val detailsJson = o.details.map { details =>
        Json.obj("details" -> Json.toJson(details))
      }.getOrElse(Json.obj())

      Json.obj(
        "type" -> "organisation",
        "utr" -> o.utr
      ) ++ detailsJson
    }
  }
}
