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

import models.{Country, InternationalAddress}

object InternationalAddressBuilder {

  val anInternationalAddress: InternationalAddress = InternationalAddress(
    line1 = "default-address-line1",
    line2 = None,
    city = "default-city",
    region = Some("default-region"),
    postal = Some("default-postal-code"),
    country = Country("US", "United States")
  )
}