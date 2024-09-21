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

import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration

class AppConfigSpec extends AnyFreeSpec with Matchers with MockitoSugar {

  private val mockConfiguration = mock[Configuration]

  private val underTest = AppConfig(mockConfiguration)

  ".auditSource" - {
    "must return the auditing source from the application.conf file" in {
      when(mockConfiguration.get[String]("auditing.auditSource")).thenReturn("some-audit-source")

      underTest.auditSource mustBe "some-audit-source"
    }
  }

  ".manageFrontendUrl" - {
    "must return Manage Frontend service URL from the application.conf file" in {
      when(mockConfiguration.get[Service]("microservice.services.digital-platform-reporting-manage-frontend"))
        .thenReturn(Service("manage-frontend-url", "20006", "http"))

      underTest.manageFrontendUrl mustBe "http://manage-frontend-url:20006/digital-platform-reporting/manage-reporting"
    }
  }

  ".platformOperatorFrontendUrl" - {
    "must return Platform Operator Frontend service URL from the application.conf file" in {
      when(mockConfiguration.get[Service]("microservice.services.digital-platform-reporting-operator-frontend"))
        .thenReturn(Service("platform-operator-frontend-url", "20005", "http"))

      underTest.platformOperatorFrontendUrl mustBe "http://platform-operator-frontend-url:20005/digital-platform-reporting"
    }
  }

  ".addPlatformOperatorUrl" - {
    "must return a URL to start the process of adding a Platform Operator" in {
      when(mockConfiguration.get[Service]("microservice.services.digital-platform-reporting-operator-frontend"))
        .thenReturn(Service("platform-operator-frontend-url", "20005", "http"))

      underTest.addPlatformOperatorUrl mustBe "http://platform-operator-frontend-url:20005/digital-platform-reporting/platform-operator/add-platform-operator/start"
    }
  }
}
