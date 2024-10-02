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
import builders.EnrolmentDetailsBuilder.anEnrolmentDetails
import builders.GroupEnrolmentBuilder.aGroupEnrolment
import org.scalatest.TryValues
import play.api.libs.json.Json

class GroupEnrolmentSpec extends SpecBase with TryValues {

  "a GroupEnrolment" - {
    "must write correct json" in {
      val groupEnrolment = aGroupEnrolment.copy(providerId = "some-internal-id")
      val json = Json.toJson(groupEnrolment)

      json mustBe Json.obj(
        "userId" -> "some-internal-id",
        "type" -> "principal",
        "action" -> "enrolAndActivate"
      )
    }
  }

  ".apply(enrolmentDetails: EnrolmentDetails)" - {
    "must create GroupEnrolment from EnrolmentDetails" in {
      GroupEnrolment.apply(anEnrolmentDetails) mustBe GroupEnrolment(
        providerId = anEnrolmentDetails.providerId,
        groupId = anEnrolmentDetails.groupId,
        identifier = anEnrolmentDetails.identifier,
      )
    }
  }
}
