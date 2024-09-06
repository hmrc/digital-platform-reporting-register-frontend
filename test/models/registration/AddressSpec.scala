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

import builders.BusinessAddressBuilder.aBusinessAddress
import builders.UkAddressBuilder.aUkAddress
import builders.InternationalAddressBuilder.anInternationalAddress
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class AddressSpec extends AnyFreeSpec with Matchers {

  private val underTest = Address

  ".apply(businessAddress: BusinessAddress)" - {
    "must map businessAddress to Address" in {
      underTest.apply(aBusinessAddress) mustBe Address(
        addressLine1 = aBusinessAddress.addressLine1,
        addressLine2 = aBusinessAddress.addressLine2,
        addressLine3 = Some(aBusinessAddress.city),
        addressLine4 = aBusinessAddress.region,
        postalCode = aBusinessAddress.postalCode,
        countryCode = aBusinessAddress.country.code
      )
    }
  }

  ".fromUkAddress" - {
    "must map a UK address to Address" in {
      underTest.fromUkAddress(aUkAddress) mustBe Address(
        addressLine1 = aUkAddress.line1,
        addressLine2 = aUkAddress.line2,
        addressLine3 = Some(aUkAddress.town),
        addressLine4 = aUkAddress.county,
        postalCode = aUkAddress.postCode,
        countryCode = aUkAddress.country.code
      )
    }
  }

  ".fromInternationalAddress" - {
    "must map an international address to Address" in {
      underTest.fromInternationalAddress(anInternationalAddress) mustBe Address(
        addressLine1 = anInternationalAddress.line1,
        addressLine2 = anInternationalAddress.line2,
        addressLine3 = Some(anInternationalAddress.city),
        addressLine4 = anInternationalAddress.region,
        postalCode = anInternationalAddress.postal,
        countryCode = anInternationalAddress.country.code
      )
    }
  }
}
