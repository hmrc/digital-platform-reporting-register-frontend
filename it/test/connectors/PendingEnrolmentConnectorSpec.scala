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
import builders.PendingEnrolmentRequestBuilder.aPendingEnrolmentRequest
import com.github.tomakehurst.wiremock.client.WireMock.*
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class PendingEnrolmentConnectorSpec extends ConnectorSpecBase {

  private lazy val app: Application = new GuiceApplicationBuilder()
    .configure("microservice.services.digital-platform-reporting.port" -> wireMockPort)
    .build()

  private lazy val underTest = app.injector.instanceOf[PendingEnrolmentConnector]

  ".upsert" - {
    "must succeed when the server returns OK" in {
      wireMockServer
        .stubFor(post(urlMatching("/digital-platform-reporting/pending-enrolment"))
          .willReturn(ok()))

      underTest.save(aPendingEnrolmentRequest).futureValue
    }

    "must return a failed future when the server returns an error" in {
      wireMockServer
        .stubFor(post(urlMatching("/digital-platform-reporting/pending-enrolment"))
          .willReturn(badRequest()))

      underTest.save(aPendingEnrolmentRequest).failed.futureValue
    }
  }
}
