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

import base.SpecBase
import builders.BusinessAddressBuilder.aBusinessAddress
import builders.InternationalAddressBuilder.anInternationalAddress
import builders.JerseyGuernseyIoMAddressBuilder.aJerseyGuernseyIsleOfManAddress
import builders.UkAddressBuilder.aUkAddress

class AddressSpec extends SpecBase {

  private val underTest = Address

  ".apply(businessAddress: BusinessAddress)" - {
    "must map businessAddress to Address" in {
      underTest.apply(aBusinessAddress) mustBe Address(
        addressLine1 = aBusinessAddress.addressLine1,
        addressLine2 = aBusinessAddress.addressLine2,
        addressLine3 = Some(aBusinessAddress.city),
        addressLine4 = aBusinessAddress.region,
        postalCode = Some(aBusinessAddress.postalCode),
        countryCode = aBusinessAddress.country.code
      )
    }
  }

  ".apply(ukAddress: UkAddress)" - {
    "must map a UK address to Address" in {
      underTest.apply(aUkAddress) mustBe Address(
        addressLine1 = aUkAddress.line1,
        addressLine2 = aUkAddress.line2,
        addressLine3 = Some(aUkAddress.town),
        addressLine4 = aUkAddress.county,
        postalCode = Some(aUkAddress.postCode),
        countryCode = aUkAddress.country.code
      )
    }
  }

  ".apply(jerseyGuernseyIoMAddress: JerseyGuernseyIoMAddress)" - {
    "must map a JerseyGuernseyIoM address to Address" in {
      underTest.apply(aJerseyGuernseyIsleOfManAddress) mustBe Address(
        addressLine1 = aJerseyGuernseyIsleOfManAddress.line1,
        addressLine2 = aJerseyGuernseyIsleOfManAddress.line2,
        addressLine3 = Some(aJerseyGuernseyIsleOfManAddress.town),
        addressLine4 = aJerseyGuernseyIsleOfManAddress.county,
        postalCode = Some(aJerseyGuernseyIsleOfManAddress.postCode),
        countryCode = aJerseyGuernseyIsleOfManAddress.country.code
      )
    }
  }

  ".apply(internationalAddress: InternationalAddress)" - {
    "must map an international address to Address" in {
      underTest.apply(anInternationalAddress) mustBe Address(
        addressLine1 = anInternationalAddress.line1,
        addressLine2 = anInternationalAddress.line2,
        addressLine3 = Some(anInternationalAddress.city),
        addressLine4 = anInternationalAddress.region,
        postalCode = Some(anInternationalAddress.postal),
        countryCode = anInternationalAddress.country.code
      )
    }
  }
}
