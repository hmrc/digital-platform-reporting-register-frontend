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

package models.subscription

import base.SpecBase
import models.{IndividualName, SoleTraderName}

class IndividualSpec extends SpecBase {

  private val underTest = Individual

  "must map IndividualName to Individual" in {
    underTest.apply(IndividualName("any-first-name", "any-last-name")) mustBe
      Individual("any-first-name", "any-last-name")
  }

  "must map SoleTraderName to Individual" in {
    underTest.apply(SoleTraderName("any-first-name", "any-last-name")) mustBe
      Individual("any-first-name", "any-last-name")
  }
}
