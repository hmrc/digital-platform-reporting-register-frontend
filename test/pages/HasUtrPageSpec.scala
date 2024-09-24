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
import models.BusinessType.{Individual, SoleTrader}
import models.{BusinessType, CheckMode, Nino, NormalMode, Utr}
import org.scalatest.{OptionValues, TryValues}

class HasUtrPageSpec extends SpecBase with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to UTR when the answer is yes" in {
        val answers = anEmptyAnswer.set(HasUtrPage, true).success.value
        HasUtrPage.nextPage(NormalMode, answers) mustEqual routes.UtrController.onPageLoad(NormalMode)
      }

      "when the answer is no" - {
        "must go to business name when the user is not an individual or a sole trader" in {
          val businessTypes = BusinessType.values.filterNot(x => x == Individual | x == SoleTrader)

          for (businessType <- businessTypes) {
            val answers = anEmptyAnswer
              .set(HasUtrPage, false).success.value
              .set(BusinessTypePage, businessType).success.value

            HasUtrPage.nextPage(NormalMode, answers) mustEqual routes.BusinessNameNoUtrController.onPageLoad(NormalMode)
          }
        }

        "must go to Has Nino when the user is an individual or sole trader and we do not know their NINO" in {
          val businessTypes = Seq(Individual, SoleTrader)

          for (businessType <- businessTypes) {
            val answers = anEmptyAnswer
              .set(HasUtrPage, false).success.value
              .set(BusinessTypePage, businessType).success.value

            HasUtrPage.nextPage(NormalMode, answers) mustEqual routes.HasNinoController.onPageLoad(NormalMode)
          }
        }

        "must go to Individual Name when the user is an individual or sole trader and we know their NINO" in {
          val businessTypes = Seq(Individual, SoleTrader)

          for (businessType <- businessTypes) {
            val answers = anEmptyAnswer.copy(user = aUser.copy(taxIdentifier = Some(Nino("nino"))))
              .set(HasUtrPage, false).success.value
              .set(BusinessTypePage, businessType).success.value

            HasUtrPage.nextPage(NormalMode, answers) mustEqual routes.IndividualNameController.onPageLoad(NormalMode)
          }
        }

        "must go to Has Nino when the user is an individual or sole trader and we know their UTR instead of their NINO" in {
          val businessTypes = Seq(Individual, SoleTrader)

          for (businessType <- businessTypes) {
            val answers = anEmptyAnswer.copy(user = aUser.copy(taxIdentifier = Some(Utr("utr"))))
              .set(HasUtrPage, false).success.value
              .set(BusinessTypePage, businessType).success.value

            HasUtrPage.nextPage(NormalMode, answers) mustEqual routes.HasNinoController.onPageLoad(NormalMode)
          }
        }
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        HasUtrPage.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}

