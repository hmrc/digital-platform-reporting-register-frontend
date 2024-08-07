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

import controllers.routes
import models.{BusinessType, CheckMode, Nino, NormalMode, UserAnswers, Utr}
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class BusinessTypePageSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  ".nextPage" - {

    val emptyAnswers = UserAnswers("id", None)

    "in Normal Mode" - {

      "must go to Individual Name for an Individual business type when we know their NINO" in {

        val answersWithNino = emptyAnswers.copy(taxIdentifier = Some(Nino("nino")))
        val answers = answersWithNino.set(BusinessTypePage, BusinessType.Individual).success.value
        BusinessTypePage.nextPage(NormalMode, answers) mustEqual routes.IndividualNameController.onPageLoad(NormalMode)
      }

      "must go to Has Nino for an Individual business type when we do not know their NINO" in {

        val answers = emptyAnswers.set(BusinessTypePage, BusinessType.Individual).success.value
        BusinessTypePage.nextPage(NormalMode, answers) mustEqual routes.HasNinoController.onPageLoad(NormalMode)
      }


      "must go to Has Nino for an Individual business type when we know their UTR instead of their NINO" in {

        val answersWithUtr = emptyAnswers.copy(taxIdentifier = Some(Utr("utr")))
        val answers = answersWithUtr.set(BusinessTypePage, BusinessType.Individual).success.value
        BusinessTypePage.nextPage(NormalMode, answers) mustEqual routes.HasNinoController.onPageLoad(NormalMode)
      }

      "must go to Registered in UK for all other business types" in {

        for (businessType <- BusinessType.values.filterNot(_ == BusinessType.Individual)) {
          
          val answers = emptyAnswers.set(BusinessTypePage, businessType).success.value
          BusinessTypePage.nextPage(NormalMode, answers) mustEqual routes.RegisteredInUkController.onPageLoad(NormalMode)
        }
      }
      
      "must go to Journey Recovery if business type has not been answered" in {
        
        BusinessTypePage.nextPage(NormalMode, emptyAnswers) mustEqual routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        BusinessTypePage.nextPage(CheckMode, emptyAnswers) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
