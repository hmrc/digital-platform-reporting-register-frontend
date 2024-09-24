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

package pages

import base.SpecBase
import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.{CheckMode, NormalMode}

class PrimaryContactNamePageSpec extends SpecBase {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Primary Contact Email Address" in {
        PrimaryContactNamePage.nextPage(NormalMode, anEmptyAnswer) mustEqual routes.PrimaryContactEmailAddressController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        PrimaryContactNamePage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
