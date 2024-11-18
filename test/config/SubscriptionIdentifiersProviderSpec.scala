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

package config

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.inject.guice.GuiceApplicationBuilder

class SubscriptionIdentifiersProviderSpec extends AnyFreeSpec with Matchers with OptionValues {
  
  "subscriptionIdentifiers" - {

    "must be empty when there are no details in config" in {

      val app =
        new GuiceApplicationBuilder()
          .build()

      val provider = app.injector.instanceOf[SubscriptionIdentifiersProvider]
      
      provider.subscriptionIdentifiers must not be defined
    }
  }

  "must return Subscription Identifiers when the encrypted values are present in config" in {

    val safeId = "XYZ789"
    val dprsId = "ABC123"
    
    val app =
      new GuiceApplicationBuilder()
        .configure("subscriptionIdentifiers.safeId" -> safeId)
        .configure("subscriptionIdentifiers.dprsId" -> dprsId)
        .build()

    val provider = app.injector.instanceOf[SubscriptionIdentifiersProvider]
    
    provider.subscriptionIdentifiers.value mustEqual SubscriptionIdentifiers(safeId, dprsId)
  }
}
