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

import base.SpecBase
import builders.AddressBuilder.anAddress
import builders.SubscriptionRequestBuilder.aSubscriptionRequest
import builders.UserAnswersBuilder.aUserAnswers
import models.BusinessType.*
import models.RegistrationType.{PlatformOperator, ThirdParty}
import models.registration.responses.{MatchResponseWithId, MatchResponseWithoutId, NoMatchResponse}
import models.subscription.requests.SubscriptionRequest
import models.subscription.responses.SubscribedResponse
import models.subscription.{Individual, IndividualContact}
import org.scalactic.source.Position
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.{BusinessNameNoUtrPage, BusinessTypePage, IsThisYourBusinessPage, RegistrationTypePage}
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, SymmetricCryptoFactory}

import java.security.SecureRandom
import java.time.Instant
import java.util.Base64

class SubscriptionDetailsSpec extends SpecBase {

  private val aesKey = {
    val aesKey = new Array[Byte](32)
    new SecureRandom().nextBytes(aesKey)
    Base64.getEncoder.encodeToString(aesKey)
  }

  private val configuration = Configuration("crypto.key" -> aesKey)

  private implicit val crypto: Encrypter with Decrypter =
    SymmetricCryptoFactory.aesGcmCryptoFromConfig("crypto", configuration.underlying)

  "Subscription Details" - {
    "must serialise/deserialise to/from JSON with encrypted formats" in {
      val request = SubscriptionRequest("safeId", true, None, IndividualContact(Individual("first", "last"), "email", None), None)
      val response = SubscribedResponse("dprsId", Instant.now())
      val subscriptionDetails = SubscriptionDetails(response, request, RegistrationType.PlatformOperator, None, None, false)

      val json = Json.toJson(subscriptionDetails)(SubscriptionDetails.encryptedFormat(implicitly))
      json.as[SubscriptionDetails](SubscriptionDetails.encryptedFormat(implicitly)) mustEqual subscriptionDetails
    }

    "must correctly create SubscriptionDetails when Org, MatchResponseWithID" in {
      val registrationResponse = MatchResponseWithId("safeId", anAddress, Some("Testing 1 Ltd"))
      val response = SubscribedResponse("dprsId", Instant.parse("2024-03-17T09:30:47Z"))
      val request = aSubscriptionRequest
      val userAnswers = aUserAnswers
        .copy(registrationResponse = Some(registrationResponse))
        .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
        .set(BusinessTypePage, BusinessType.AssociationOrTrust).success.value
      val subscriptionDetails = SubscriptionDetails(response, request, PlatformOperator, Some(AssociationOrTrust), Some("Testing 1 Ltd"), false)

      SubscriptionDetails.apply(response, request, userAnswers, false) mustEqual subscriptionDetails
    }

    "must correctly create SubscriptionDetails when Org, MatchResponseWithoutID" in {
      val registrationResponse = MatchResponseWithoutId("safeId")
      val response = SubscribedResponse("dprsId", Instant.parse("2024-03-17T09:30:47Z"))
      val request = aSubscriptionRequest
      val userAnswers = aUserAnswers
        .copy(registrationResponse = Some(registrationResponse))
        .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
        .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
        .set(BusinessNameNoUtrPage, "Testing 2 Ltd").success.value
      val subscriptionDetails = SubscriptionDetails(response, request, PlatformOperator, Some(LimitedCompany), Some("Testing 2 Ltd"), false)

      SubscriptionDetails.apply(response, request, userAnswers, false) mustEqual subscriptionDetails
    }

    "must correctly create SubscriptionDetails ThirdParty Individual, MatchResponseWithoutId" in {
      val registrationResponse = MatchResponseWithoutId("safeId")
      val response = SubscribedResponse("dprsId", Instant.parse("2024-03-17T09:30:47Z"))
      val request = aSubscriptionRequest
      val userAnswers = aUserAnswers
        .copy(registrationResponse = Some(registrationResponse))
        .set(RegistrationTypePage, RegistrationType.ThirdParty).success.value
        .set(BusinessTypePage, BusinessType.Individual).success.value
      val subscriptionDetails = SubscriptionDetails(response, request, ThirdParty, Some(BusinessType.Individual), None, false)

      SubscriptionDetails.apply(response, request, userAnswers, false) mustEqual subscriptionDetails
    }

    "must correctly create SubscriptionDetails when ThirdParty Sole Trader, MatchResponseWithoutId" in {
      val registrationResponse = MatchResponseWithoutId("safeId")
      val response = SubscribedResponse("dprsId", Instant.parse("2024-03-17T09:30:47Z"))
      val request = aSubscriptionRequest
      val userAnswers = aUserAnswers
        .copy(registrationResponse = Some(registrationResponse))
        .set(RegistrationTypePage, RegistrationType.ThirdParty).success.value
        .set(BusinessTypePage, BusinessType.SoleTrader).success.value
      val subscriptionDetails = SubscriptionDetails(response, request, ThirdParty, Some(BusinessType.SoleTrader), None, false)

      SubscriptionDetails.apply(response, request, userAnswers, false) mustEqual subscriptionDetails
    }

    "must correctly create SubscriptionDetails when ThirdParty Sole Trader, MatchResponseWithId" in {
      val registrationResponse = MatchResponseWithId("safeId", anAddress, Some("Testing 1 Ltd"))
      val response = SubscribedResponse("dprsId", Instant.parse("2024-03-17T09:30:47Z"))
      val request = aSubscriptionRequest
      val userAnswers = aUserAnswers
        .copy(registrationResponse = Some(registrationResponse))
        .set(RegistrationTypePage, RegistrationType.ThirdParty).success.value
        .set(BusinessTypePage, BusinessType.SoleTrader).success.value
      val subscriptionDetails = SubscriptionDetails(response, request, ThirdParty, Some(BusinessType.SoleTrader), None, false)

      SubscriptionDetails.apply(response, request, userAnswers, false) mustEqual subscriptionDetails
    }

    "must correctly create SubscriptionDetails when ThirdParty Individual, MatchResponseWithId" in {
      val registrationResponse = MatchResponseWithId("safeId", anAddress, Some("Testing 1 Ltd"))
      val response = SubscribedResponse("dprsId", Instant.parse("2024-03-17T09:30:47Z"))
      val request = aSubscriptionRequest
      val userAnswers = aUserAnswers
        .copy(registrationResponse = Some(registrationResponse))
        .set(RegistrationTypePage, RegistrationType.ThirdParty).success.value
        .set(BusinessTypePage, BusinessType.Individual).success.value
      val subscriptionDetails = SubscriptionDetails(response, request, ThirdParty, Some(BusinessType.Individual), None, false)

      SubscriptionDetails.apply(response, request, userAnswers, false) mustEqual subscriptionDetails
    }

    "must correctly create SubscriptionDetails when ThirdParty Org, MatchResponseWithoutId" in {
      val registrationResponse = MatchResponseWithoutId("safeId")
      val response = SubscribedResponse("dprsId", Instant.parse("2024-03-17T09:30:47Z"))
      val request = aSubscriptionRequest
      val userAnswers = aUserAnswers
        .copy(registrationResponse = Some(registrationResponse))
        .set(RegistrationTypePage, RegistrationType.ThirdParty).success.value
        .set(BusinessTypePage, BusinessType.Partnership).success.value
        .set(BusinessNameNoUtrPage, "Testing 3 Ltd").success.value
      val subscriptionDetails = SubscriptionDetails(response, request, ThirdParty, Some(BusinessType.Partnership), Some("Testing 3 Ltd"), true)

      SubscriptionDetails.apply(response, request, userAnswers, true) mustEqual subscriptionDetails
    }

    "must correctly create SubscriptionDetails when ThirdParty Individual MatchResponseWithId" in {
      val registrationResponse = MatchResponseWithId("safeId", anAddress, Some("Testing 4 Ltd"))
      val response = SubscribedResponse("dprsId", Instant.parse("2024-03-17T09:30:47Z"))
      val request = aSubscriptionRequest
      val userAnswers = aUserAnswers
        .copy(registrationResponse = Some(registrationResponse))
        .set(RegistrationTypePage, RegistrationType.ThirdParty).success.value
        .set(BusinessTypePage, BusinessType.Llp).success.value
      val subscriptionDetails = SubscriptionDetails(response, request, ThirdParty, Some(BusinessType.Llp), Some("Testing 4 Ltd"), false)

      SubscriptionDetails.apply(response, request, userAnswers, false) mustEqual subscriptionDetails
    }

    "must correctly create SubscriptionDetails when Auto-matched enrollment" in {
      val registrationResponse = MatchResponseWithId("safeId", anAddress, Some("Testing 5 Ltd"))
      val response = SubscribedResponse("dprsId", Instant.parse("2024-03-17T09:30:47Z"))
      val request = aSubscriptionRequest
      val userAnswers = aUserAnswers
        .copy(registrationResponse = Some(registrationResponse))
        .set(RegistrationTypePage, RegistrationType.ThirdParty).success.value
        .set(IsThisYourBusinessPage, true).success.value
      val subscriptionDetails = SubscriptionDetails(response, request, ThirdParty, None, Some("Testing 5 Ltd"), false)

      SubscriptionDetails.apply(response, request, userAnswers, false) mustEqual subscriptionDetails
    }

    "must correctly create SubscriptionDetails when registrationResponse is not a Match" in {
      val registrationResponse = NoMatchResponse()
      val response = SubscribedResponse("dprsId", Instant.parse("2024-03-17T09:30:47Z"))
      val request = aSubscriptionRequest
      val userAnswers = aUserAnswers
        .copy(registrationResponse = Some(registrationResponse))
        .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
        .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
      val subscriptionDetails = SubscriptionDetails(response, request, PlatformOperator, Some(LimitedCompany), None, false)

      SubscriptionDetails.apply(response, request, userAnswers, false) mustEqual subscriptionDetails
    }
  }
}
