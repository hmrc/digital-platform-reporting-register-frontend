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
import builders.OrganisationWithoutIdBuilder.anOrganisationWithoutId
import com.github.tomakehurst.wiremock.client.WireMock.*
import models.registration.requests.*
import models.registration.responses.*
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json

class RegistrationConnectorSpec extends ConnectorSpecBase {

  private lazy val app: Application = new GuiceApplicationBuilder()
    .configure("microservice.services.digital-platform-reporting.port" -> wireMockPort)
    .build()

  private lazy val underTest = app.injector.instanceOf[RegistrationConnector]

  ".register" - {
    "must return a registration response when the server returns OK" in {
      val response = MatchResponseWithoutId("safeId")

      wireMockServer.stubFor(
        post(urlMatching("/digital-platform-reporting/register"))
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result = underTest.register(anOrganisationWithoutId).futureValue

      result mustEqual response
    }

    "must return a `no match` response when the server returns NOT_FOUND" in {
      val request = OrganisationWithUtr("utr", None)
      val response = NoMatchResponse()

      wireMockServer.stubFor(
        post(urlMatching("/digital-platform-reporting/register"))
          .willReturn(notFound())
      )

      val result = underTest.register(request).futureValue

      result mustEqual response
    }

    "must return an `already subscribed` response when the server returns CONFLICT" in {
      val request = OrganisationWithUtr("utr", None)
      val response = AlreadySubscribedResponse()

      wireMockServer.stubFor(
        post(urlMatching("/digital-platform-reporting/register"))
          .willReturn(aResponse().withStatus(409))
      )

      val result = underTest.register(request).futureValue

      result mustEqual response
    }

    "must return a failed future when the server returns an error" in {
      wireMockServer
        .stubFor(post(urlMatching("/digital-platform-reporting/register"))
          .willReturn(serverError()))

      underTest.register(anOrganisationWithoutId).failed.futureValue
    }
  }
}
