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

package builders

import models.registration.Address

object AddressBuilder {

  val anAddress: Address = Address(
    addressLine1 = "default-address-line1",
    addressLine2 = Some("default-address-line2"),
    addressLine3 = Some("default-address-line3"),
    addressLine4 = Some("default-address-line4"),
    postalCode = None,
    countryCode = "UK"
  )

  val anyAddress: Address = anAddress
}
