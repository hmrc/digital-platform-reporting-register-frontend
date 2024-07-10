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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import java.time.LocalDate

final case class IndividualWithNino(nino: String, details: IndividualDetails, dateOfBirth: LocalDate) extends RegistrationRequest

object IndividualWithNino {
  
  implicit lazy val writes: OWrites[IndividualWithNino] =
    (
      (__ \ "nino").write[String] and
      (__ \ "firstName").write[String] and
      (__ \ "lastName").write[String] and
      (__ \ "dateOfBirth").write[LocalDate]
    )(i => (i.nino, i.details.firstName, i.details.lastName, i.dateOfBirth))
}
