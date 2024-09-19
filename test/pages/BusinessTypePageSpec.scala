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
import builders.UserBuilder.aUser
import controllers.routes
import models.{BusinessType, CheckMode, Nino, NormalMode, Utr}
import org.scalatest.{OptionValues, TryValues}

class BusinessTypePageSpec extends SpecBase with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Individual Name for an Individual business type when we know their NINO" in {
        val answersWithNino = anEmptyAnswer.copy(user = aUser.copy(taxIdentifier = Some(Nino("nino"))))
        val answers = answersWithNino.set(BusinessTypePage, BusinessType.Individual).success.value
        BusinessTypePage.nextPage(NormalMode, answers) mustEqual routes.IndividualNameController.onPageLoad(NormalMode)
      }

      "must go to Has Nino for an Individual business type when we do not know their NINO" in {
        val answers = anEmptyAnswer.set(BusinessTypePage, BusinessType.Individual).success.value
        BusinessTypePage.nextPage(NormalMode, answers) mustEqual routes.HasNinoController.onPageLoad(NormalMode)
      }


      "must go to Has Nino for an Individual business type when we know their UTR instead of their NINO" in {
        val answersWithUtr = anEmptyAnswer.copy(user = aUser.copy(taxIdentifier = Some(Utr("utr"))))
        val answers = answersWithUtr.set(BusinessTypePage, BusinessType.Individual).success.value
        BusinessTypePage.nextPage(NormalMode, answers) mustEqual routes.HasNinoController.onPageLoad(NormalMode)
      }

      "must go to Registered in UK for all other business types" in {

        for (businessType <- BusinessType.values.filterNot(_ == BusinessType.Individual)) {

          val answers = anEmptyAnswer.set(BusinessTypePage, businessType).success.value
          BusinessTypePage.nextPage(NormalMode, answers) mustEqual routes.RegisteredInUkController.onPageLoad(NormalMode)
        }
      }

      "must go to Journey Recovery if business type has not been answered" in {

        BusinessTypePage.nextPage(NormalMode, anEmptyAnswer) mustEqual routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "in Check Mode" - {

      "must go to Check Answers" in {

        BusinessTypePage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
