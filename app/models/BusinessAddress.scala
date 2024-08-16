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

package models

import models.Country.allCountries
import models.registration.Address
import play.api.libs.json.*

case class BusinessAddress(addressLine1: String,
                           addressLine2: Option[String] = None,
                           city: String,
                           region: Option[String] = None,
                           postalCode: Option[String] = None,
                           country: Country)

object BusinessAddress {

  implicit val format: OFormat[BusinessAddress] = Json.format

  implicit class BusinessAddressOps(address: BusinessAddress) {

    def toAddress: Address = {
      Address(
        addressLine1 = address.addressLine1,
        addressLine2 = address.addressLine2,
        addressLine3 = Some(address.city),
        addressLine4 = None,
        postalCode = address.postalCode,
        countryCode = allCountries
          .find(_ == address.country)
          .map(_.code)
          .getOrElse("GB")
      )
    }
  }
}
