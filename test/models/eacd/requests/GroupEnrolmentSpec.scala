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

package models.eacd.requests

import base.SpecBase
import builders.GroupEnrolmentBuilder.aGroupEnrolment
import models.eacd.Identifier
import play.api.libs.json.Json

class GroupEnrolmentSpec extends SpecBase {

  ".enrolmentKey" - {
    "must generate correct enrolmentKey when identifier provided" in {
      val identifier = Identifier("some-key", "some-value")
      val underTest = aGroupEnrolment.copy(identifier = Some(identifier))

      underTest.enrolmentKey mustBe "HMRC-DPRS~some-key~some-value"
    }

    "must generate correct enrolmentKey when identifier is None" in {
      val underTest = aGroupEnrolment.copy(identifier = None)

      underTest.enrolmentKey mustBe "HMRC-DPRS~"
    }
  }

  "a GroupEnrolment" - {
    "must write correct json" in {
      val groupEnrolment = aGroupEnrolment.copy(userId = "some-internal-id")
      val json = Json.toJson(groupEnrolment)

      json mustBe Json.obj(
        "userId" -> "some-internal-id",
        "type" -> "principal",
        "action" -> "enrolAndActivate"
      )
    }
  }
}
