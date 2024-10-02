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

package models.eacd.requests

import base.SpecBase
import builders.BusinessAddressBuilder.aBusinessAddress
import builders.InternationalAddressBuilder.anInternationalAddress
import builders.UkAddressBuilder.aUkAddress
import builders.UserAnswersBuilder.aUserAnswers
import builders.UserBuilder.aUser
import models.eacd.EnrolmentKnownFacts
import models.{Nino, Utr}
import org.scalatest.TryValues
import pages.*

class EnrolmentKnownFactsSpec extends SpecBase with TryValues {

  ".apply(...)" - {
    "must return correct EnrolmentKnownFacts when taxIdentifier is Nino" in {
      val userAnswers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = Some(Nino("some-nino"))))

      EnrolmentKnownFacts.apply(userAnswers).get mustBe EnrolmentKnownFacts(
        providerId = userAnswers.user.providerId.get,
        verifierKey = "NINO",
        verifierValue = "some-nino",
        groupId = userAnswers.user.groupId.get
      )
    }

    "must return correct EnrolmentKnownFacts when taxIdentifier is UTR" in {
      val userAnswers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = Some(Utr("some-utr"))))

      EnrolmentKnownFacts.apply(userAnswers).get mustBe EnrolmentKnownFacts(
        providerId = userAnswers.user.providerId.get,
        verifierKey = "UTR",
        verifierValue = "some-utr",
        groupId = userAnswers.user.groupId.get
      )
    }

    "must return correct EnrolmentKnownFacts when user answers is Nino" in {
      val userAnswers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(NinoPage, "some-nino").success.value

      EnrolmentKnownFacts.apply(userAnswers).get mustBe EnrolmentKnownFacts(
        providerId = userAnswers.user.providerId.get,
        verifierKey = "NINO",
        verifierValue = "some-nino",
        groupId = userAnswers.user.groupId.get
      )
    }

    "must return correct EnrolmentKnownFacts when user answers have UTR" in {
      val userAnswers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(UtrPage, "some-utr").success.value

      EnrolmentKnownFacts.apply(userAnswers).get mustBe EnrolmentKnownFacts(
        providerId = userAnswers.user.providerId.get,
        verifierKey = "UTR",
        verifierValue = "some-utr",
        groupId = userAnswers.user.groupId.get
      )
    }

    "must return correct EnrolmentKnownFacts when no Nino or Utr, and user answers has BusinessAddress" in {
      val userAnswers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessAddressPage, aBusinessAddress).success.value

      EnrolmentKnownFacts.apply(userAnswers).get mustBe EnrolmentKnownFacts(
        providerId = userAnswers.user.providerId.get,
        verifierKey = "Postcode",
        verifierValue = aBusinessAddress.postalCode,
        groupId = userAnswers.user.groupId.get
      )
    }

    "must return correct EnrolmentKnownFacts when no Nino or Utr, and user answers has UkAddress" in {
      val userAnswers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(UkAddressPage, aUkAddress).success.value

      EnrolmentKnownFacts.apply(userAnswers).get mustBe EnrolmentKnownFacts(
        providerId = userAnswers.user.providerId.get,
        verifierKey = "Postcode",
        verifierValue = aUkAddress.postCode,
        groupId = userAnswers.user.groupId.get
      )
    }

    "must return correct EnrolmentKnownFacts when no Nino or Utr, and user answers has InternationalAddressPage" in {
      val userAnswers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(InternationalAddressPage, anInternationalAddress).success.value

      EnrolmentKnownFacts.apply(userAnswers).get mustBe EnrolmentKnownFacts(
        providerId = userAnswers.user.providerId.get,
        verifierKey = "Postcode",
        verifierValue = anInternationalAddress.postal,
        groupId = userAnswers.user.groupId.get
      )
    }

    "must return correct None when no Nino, no Utr, no Address" in {
      val userAnswers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))

      EnrolmentKnownFacts.apply(userAnswers) mustBe None
    }
  }
}
