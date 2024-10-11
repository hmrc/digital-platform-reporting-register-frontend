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

package connectors

import base.ConnectorSpecBase
import com.github.tomakehurst.wiremock.client.WireMock.*
import models.subscription.*
import models.subscription.requests.*
import models.subscription.responses.*
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

import java.time.Instant

class SubscriptionConnectorSpec extends ConnectorSpecBase {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure("microservice.services.digital-platform-reporting.port" -> wireMockPort)
    .build()

  private lazy val underTest = app.injector.instanceOf[SubscriptionConnector]

  ".subscribe" - {
    "must return a subscription response when the server returns OK" in {
      val contact = OrganisationContact(Organisation("name"), "email", None)
      val request = SubscriptionRequest("safe", true, None, contact, None)
      val response = SubscribedResponse("DPRS123", Instant.parse("2024-03-17T09:30:47Z"))

      wireMockServer.stubFor(
        post(urlMatching("/digital-platform-reporting/subscribe"))
          .withRequestBody(equalTo(Json.toJson(request).toString))
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result = underTest.subscribe(request).futureValue

      result mustEqual response
    }

    "must return Already Subscribed when the server returns CONFLICT" in {
      val contact = OrganisationContact(Organisation("name"), "email", None)
      val request = SubscriptionRequest("safe", true, None, contact, None)

      wireMockServer.stubFor(
        post(urlMatching("/digital-platform-reporting/subscribe"))
          .withRequestBody(equalTo(Json.toJson(request).toString))
          .willReturn(aResponse().withStatus(409))
      )

      val result = underTest.subscribe(request).futureValue

      result mustEqual AlreadySubscribedResponse()
    }

    "must return a failed future when the server returns an error" in {
      val contact = OrganisationContact(Organisation("name"), "email", None)
      val request = SubscriptionRequest("safe", true, None, contact, None)

      wireMockServer.stubFor(
        post(urlMatching("/digital-platform-reporting/subscribe"))
          .withRequestBody(equalTo(Json.toJson(request).toString))
          .willReturn(serverError())
      )

      underTest.subscribe(request).failed.futureValue.getMessage mustBe "Error with code: 500"
    }
  }
}
