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

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.inject.guice.GuiceApplicationBuilder

class SubscriptionIdentifiersProviderSpec extends AnyFreeSpec with Matchers with OptionValues {

  "subscriptionIdentifiers" - {
    "must be empty when there are no details in config" in {
      val app = new GuiceApplicationBuilder().build()
      val provider = app.injector.instanceOf[SubscriptionIdentifiersProvider]

      provider.subscriptionIdentifiers must not be defined
    }
  }

  "must return Subscription Identifiers when the encrypted values are present in config" in {
    val app = new GuiceApplicationBuilder()
      .configure("subscriptionIdentifiers.0.safeId" -> "safeId-0")
      .configure("subscriptionIdentifiers.0.dprsId" -> "dprsId-0")
      .configure("subscriptionIdentifiers.1.safeId" -> "safeId-1")
      .configure("subscriptionIdentifiers.1.dprsId" -> "dprsId-1")
      .configure("subscriptionIdentifiers.2.safeId" -> "safeId-2")
      .configure("subscriptionIdentifiers.2.dprsId" -> "dprsId-2")
      .build()
    val provider = app.injector.instanceOf[SubscriptionIdentifiersProvider]

    provider.subscriptionIdentifiers.value mustEqual Seq(
      SubscriptionIdentifiers("safeId-0", "dprsId-0"),
      SubscriptionIdentifiers("safeId-1", "dprsId-1"),
      SubscriptionIdentifiers("safeId-2", "dprsId-2")
    )
  }
}
