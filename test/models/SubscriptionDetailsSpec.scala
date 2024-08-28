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

import models.subscription.requests.SubscriptionRequest
import models.subscription.responses.SubscribedResponse
import models.subscription.{Individual, IndividualContact}
import org.scalactic.source.Position
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, SymmetricCryptoFactory}

import java.security.SecureRandom
import java.util.Base64

class SubscriptionDetailsSpec extends AnyFreeSpec with Matchers {

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
      val response = SubscribedResponse("dprsId")
      val subscriptionDetails = SubscriptionDetails(response, request, RegistrationType.PlatformOperator, None)
      
      val json = Json.toJson(subscriptionDetails)(SubscriptionDetails.encryptedFormat(implicitly))
      json.as[SubscriptionDetails](SubscriptionDetails.encryptedFormat(implicitly)) mustEqual subscriptionDetails
    }
  }
}
