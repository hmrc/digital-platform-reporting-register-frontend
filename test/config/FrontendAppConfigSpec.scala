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

import base.SpecBase
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration

class FrontendAppConfigSpec extends SpecBase with MockitoSugar {

  private val mockConfiguration = mock[Configuration]

  private val underTest = FrontendAppConfig(mockConfiguration)

  ".auditSource" - {
    "must return the auditing source from the application.conf file" in {
      when(mockConfiguration.get[String]("auditing.auditSource")).thenReturn("some-audit-source")

      underTest.auditSource mustBe "some-audit-source"
    }
  }
}