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

import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.NormalMode
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class ContactDetailsGuidancePageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Primary Contact Name" in {
        ContactDetailsGuidancePage.nextPage(NormalMode, anEmptyAnswer) mustEqual routes.PrimaryContactNameController.onPageLoad(NormalMode)
      }
    }
  }
}
