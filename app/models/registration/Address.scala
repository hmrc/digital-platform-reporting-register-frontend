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

package models.registration

import models.{InternationalAddress, UkAddress}
import play.api.libs.json.{Json, OFormat}

final case class Address(addressLine1: String,
                         addressLine2: Option[String],
                         addressLine3: Option[String],
                         addressLine4: Option[String],
                         postalCode: Option[String],
                         countryCode: String)

object Address {

  implicit lazy val format: OFormat[Address] = Json.format

  def apply(internationalAddress: InternationalAddress): Address = Address(
    addressLine1 = internationalAddress.line1,
    addressLine2 = internationalAddress.line2,
    addressLine3 = Some(internationalAddress.city),
    addressLine4 = internationalAddress.region,
    postalCode = internationalAddress.postal,
    countryCode = internationalAddress.country.code
  )

  def apply(ukAddress: UkAddress): Address = Address(
    addressLine1 = ukAddress.line1,
    addressLine2 = ukAddress.line2,
    addressLine3 = Some(ukAddress.town),
    addressLine4 = ukAddress.county,
    postalCode = Some(ukAddress.postCode),
    countryCode = ukAddress.country.code
  )
}

