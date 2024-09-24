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

package models.registration.responses

import base.SpecBase
import builders.AddressBuilder.anAddress
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, SymmetricCryptoFactory}

import java.security.SecureRandom
import java.util.Base64

class RegistrationResponseSpec extends SpecBase {

  "registration response" - {
    "must serialise / deserialise" - {
      "a match with Id" in {
        val response = MatchResponseWithId("safeId", anAddress, None)
        val json = Json.toJson(response)
        val result = json.as[RegistrationResponse]
        result mustEqual response
      }

      "a match without Id" in {
        val response = MatchResponseWithoutId("safeId")
        val json = Json.toJson(response)
        val result = json.as[RegistrationResponse]
        result mustEqual response
      }

      "a `no match`" in {
        val response = NoMatchResponse()
        val json = Json.toJson(response)
        val result = json.as[RegistrationResponse]
        result mustEqual response
      }

      "an `already subscribed`" in {
        val response = AlreadySubscribedResponse()
        val json = Json.toJson(response)
        val result = json.as[RegistrationResponse]
        result mustEqual response
      }
    }

    "must serialise / deserialise with encryption" - {
      val aesKey = {
        val aesKey = new Array[Byte](32)
        new SecureRandom().nextBytes(aesKey)
        Base64.getEncoder.encodeToString(aesKey)
      }

      val configuration = Configuration("crypto.key" -> aesKey)

      implicit val crypto: Encrypter with Decrypter =
        SymmetricCryptoFactory.aesGcmCryptoFromConfig("crypto", configuration.underlying)

      "a match with Id" in {
        val response = MatchResponseWithId("safeId", anAddress, None)
        val json = Json.toJson(response)(RegistrationResponse.encryptedFormat)
        val result = json.as[RegistrationResponse](RegistrationResponse.encryptedFormat)

        result mustEqual response
      }

      "a match without Id" in {
        val response = MatchResponseWithoutId("safeId")
        val json = Json.toJson(response)(RegistrationResponse.encryptedFormat)
        val result = json.as[RegistrationResponse](RegistrationResponse.encryptedFormat)
        result mustEqual response
      }

      "a `no match`" in {
        val response = NoMatchResponse()
        val json = Json.toJson(response)(RegistrationResponse.encryptedFormat)
        val result = json.as[RegistrationResponse](RegistrationResponse.encryptedFormat)
        result mustEqual response
      }

      "an `already subscribed`" in {
        val response = AlreadySubscribedResponse()
        val json = Json.toJson(response)(RegistrationResponse.encryptedFormat)
        val result = json.as[RegistrationResponse](RegistrationResponse.encryptedFormat)
        result mustEqual response
      }
    }
  }
}
