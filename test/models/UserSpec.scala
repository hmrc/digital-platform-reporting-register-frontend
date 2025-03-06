/*
 * Copyright 2025 HM Revenue & Customs
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

package models

import base.SpecBase
import builders.UserBuilder.aUser
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}

class UserSpec extends SpecBase {

  ".isIndividualAffinityGroup" - {
    "must return true when user has Individual affinityGroup" in {
      val underTest = aUser.copy(affinityGroup = Some(Individual))
      underTest.isIndividualAffinityGroup mustBe true
    }

    "must return false when" - {
      "affinityGroup is not Individual" in {
        val underTest = aUser.copy(affinityGroup = Some(Organisation))
        underTest.isIndividualAffinityGroup mustBe false
      }

      "affinityGroup is None" in {
        val underTest = aUser.copy(affinityGroup = None)
        underTest.isIndividualAffinityGroup mustBe false
      }
    }
  }

  ".isOrganisationAffinityGroup" - {
    "must return true when user has Organisation affinityGroup" in {
      val underTest = aUser.copy(affinityGroup = Some(Organisation))
      underTest.isOrganisationAffinityGroup mustBe true
    }

    "must return false when" - {
      "affinityGroup is not Organisation" in {
        val underTest = aUser.copy(affinityGroup = Some(Individual))
        underTest.isOrganisationAffinityGroup mustBe false
      }

      "affinityGroup is None" in {
        val underTest = aUser.copy(affinityGroup = None)
        underTest.isOrganisationAffinityGroup mustBe false
      }
    }
  }
}
