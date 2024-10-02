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

package models.eacd

import base.SpecBase
import builders.EnrolmentKnownFactsBuilder.anEnrolmentKnownFacts

class EnrolmentDetailsSpec extends SpecBase {

  ".apply(...)" - {
    "must create correct object" in {
      EnrolmentDetails.apply(anEnrolmentKnownFacts, "some-dprs-id") mustBe EnrolmentDetails(
        providerId = anEnrolmentKnownFacts.providerId,
        verifierKey = anEnrolmentKnownFacts.verifierKey,
        verifierValue = anEnrolmentKnownFacts.verifierValue,
        groupId = anEnrolmentKnownFacts.groupId,
        identifier = Identifier("some-dprs-id")
      )
    }
  }
}
