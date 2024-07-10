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

import com.github.tomakehurst.wiremock.client.WireMock.*
import models.registration.Address
import models.registration.requests.*
import models.registration.responses.*
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

class RegistrationConnectorSpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with WireMockSupport {

  private lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.digital-platform-reporting.port" -> wireMockPort)
      .build()
    
  private lazy val connector = app.injector.instanceOf[RegistrationConnector]
  
  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  
  ".register" - {

    "must return a registration response when the server returns OK" in {

      val address = Address("line 1", None, None, None, None, "GB")
      val request = OrganisationWithoutId("name", address)
      val response = MatchResponseWithoutId("safeId")

      wireMockServer.stubFor(
        post(urlMatching("/digital-platform-reporting/register"))
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result = connector.register(request).futureValue

      result mustEqual response
    }

    "must return a `no match` response when the server returns NOT_FOUND" in {

      val request = OrganisationWithUtr("utr", None)
      val response = NoMatchResponse()

      wireMockServer.stubFor(
        post(urlMatching("/digital-platform-reporting/register"))
          .willReturn(ok(Json.toJson(response).toString))
      )

      val result = connector.register(request).futureValue

      result mustEqual response
    }
    
    "must return a failed future when the server returns an error" in {


      val address = Address("line 1", None, None, None, None, "GB")
      val request = OrganisationWithoutId("name", address)

      wireMockServer.stubFor(
        post(urlMatching("/digital-platform-reporting/register"))
          .willReturn(serverError())
      )

      connector.register(request).failed.futureValue
    }
  }
}
