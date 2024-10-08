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

package models.enrolment.requests

import base.SpecBase
import builders.EnrolmentDetailsBuilder.anEnrolmentDetails
import builders.PendingEnrolmentRequestBuilder.aPendingEnrolmentRequest
import play.api.libs.json.Json

class PendingEnrolmentRequestSpec extends SpecBase {

  private val validJson = Json.obj(
    "verifierKey" -> aPendingEnrolmentRequest.verifierKey,
    "verifierValue" -> aPendingEnrolmentRequest.verifierValue,
    "dprsId" -> aPendingEnrolmentRequest.dprsId
  )

  "PendingEnrolmentRequest.format" - {
    "parse from json" in {
      validJson.as[PendingEnrolmentRequest] mustBe aPendingEnrolmentRequest
    }

    "parse to json" in {
      Json.toJson(aPendingEnrolmentRequest) mustBe validJson
    }
  }

  ".apply(...)" - {
    "must create PendingEnrolmentRequest from EnrolmentDetails" in {
      PendingEnrolmentRequest.apply(anEnrolmentDetails) mustBe PendingEnrolmentRequest(
        verifierKey = anEnrolmentDetails.verifierKey,
        verifierValue = anEnrolmentDetails.verifierValue,
        dprsId = anEnrolmentDetails.identifier.value
      )
    }
  }
}