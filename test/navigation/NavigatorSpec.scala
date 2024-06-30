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

package navigation

import base.SpecBase
import controllers.routes
import models.*
import pages.*

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers) mustBe routes.IndexController.onPageLoad()
      }

      "must go from Registration Type to Business Type" in {

        navigator.nextPage(RegistrationTypePage, NormalMode, emptyUserAnswers) mustEqual routes.BusinessTypeController.onPageLoad(NormalMode)
      }

      "must go from Has UTR" - {

        "to Utr when the answer is yes" in {

          val answers = emptyUserAnswers.set(HasUtrPage, true).success.value
          navigator.nextPage(HasUtrPage, NormalMode, answers) mustEqual routes.UtrController.onPageLoad(NormalMode)
        }

        "to business name when the answer is no" in {

          val answers = emptyUserAnswers.set(HasUtrPage, false).success.value
          navigator.nextPage(HasUtrPage, NormalMode, answers) mustEqual routes.BusinessNameController.onPageLoad(NormalMode)
        }
      }
      
      "must go from Business Type to Registered in UK" in {
        
        navigator.nextPage(BusinessTypePage, NormalMode, emptyUserAnswers) mustEqual routes.RegisteredInUkController.onPageLoad(NormalMode)
      }
      
      "must go from Registered in UK" - {
        
        "to UTR when the answer is yes" in {
          
          val answers = emptyUserAnswers.set(RegisteredInUkPage, true).success.value
          navigator.nextPage(RegisteredInUkPage, NormalMode, answers) mustEqual routes.UtrController.onPageLoad(NormalMode)
        }
        
        "to Has UTR when the answer is no" in {

          val answers = emptyUserAnswers.set(RegisteredInUkPage, false).success.value
          navigator.nextPage(RegisteredInUkPage, NormalMode, answers) mustEqual routes.HasUtrController.onPageLoad(NormalMode)
        }
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, emptyUserAnswers) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
