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
import builders.GroupEnrolmentBuilder.aGroupEnrolment
import com.github.tomakehurst.wiremock.client.WireMock.*
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class TaxEnrolmentConnectorSpec extends ConnectorSpecBase {

  private lazy val app: Application = new GuiceApplicationBuilder()
    .configure("microservice.services.tax-enrolments.port" -> wireMockPort)
    .build()

  private lazy val underTest = app.injector.instanceOf[TaxEnrolmentConnector]

  ".allocateEnrolmentToGroup" - {
    "must succeed when the server returns CREATED" in {
      wireMockServer
        .stubFor(post(urlMatching(s"/tax-enrolments/groups/${aGroupEnrolment.groupId}/enrolments/${aGroupEnrolment.enrolmentKey}"))
          .willReturn(created()))

      underTest.allocateEnrolmentToGroup(aGroupEnrolment).futureValue
    }

    "must succeed when the server returns CONFLICT(409)" in {
      wireMockServer
        .stubFor(post(urlMatching(s"/tax-enrolments/groups/${aGroupEnrolment.groupId}/enrolments/${aGroupEnrolment.enrolmentKey}"))
          .willReturn(aResponse.withStatus(409)))

      underTest.allocateEnrolmentToGroup(aGroupEnrolment).futureValue
    }

    "must return a failed future when the server returns an error" in {
      wireMockServer
        .stubFor(post(urlMatching(s"/tax-enrolments/groups/${aGroupEnrolment.groupId}/enrolments/${aGroupEnrolment.enrolmentKey}"))
          .willReturn(serverError()))

      underTest.allocateEnrolmentToGroup(aGroupEnrolment).failed.futureValue
    }
  }
}
