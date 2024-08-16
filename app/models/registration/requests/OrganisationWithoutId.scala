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
import models.{BusinessAddress, UserAnswers}
import models.registration.Address
import queries.Query
import play.api.libs.json.{Json, OWrites}
import pages.{BusinessAddressPage, BusinessEnterTradingNamePage, BusinessNameNoUtrPage, InternationalAddressPage}
import models.InternationalAddress.*

final case class OrganisationWithoutId(name: String, address: Address) extends RegistrationRequest

object OrganisationWithoutId {
  
  implicit lazy val writes: OWrites[OrganisationWithoutId] = Json.writes

  def build(userAnswers: UserAnswers): EitherNec[Query, OrganisationWithoutId] = {
    
    def getOrgNameWithoutId =
      userAnswers.getEither(BusinessEnterTradingNamePage) orElse userAnswers.getEither(BusinessNameNoUtrPage)
    
    (
      getOrgNameWithoutId,
      userAnswers.getEither(BusinessAddressPage),
    ).parMapN { (orgName, businessAddress) =>
      OrganisationWithoutId(orgName, businessAddress.toAddress)
    }
  }
}
