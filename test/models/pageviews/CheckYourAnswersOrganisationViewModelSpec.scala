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

package models.pageviews

import builders.UserAnswersBuilder.anEmptyAnswer
import models.registration.Address
import models.registration.responses.{MatchResponseWithId, MatchResponseWithoutId, NoMatchResponse}
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.*
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class CheckYourAnswersOrganisationViewModelSpec extends AnyFreeSpec with Matchers with OptionValues with TryValues {

  private implicit val messages: Messages = stubMessages()

  ".apply" - {

    val anyName = "name"
    val anyAddress = Address("line 1", None, None, None, None, "ZZ")

    "must build when the registration response is a match with Id containing an organisation name" in {
      val answers = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithId("safe", anyAddress, Some(anyName))))
      val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers)
      viewModel.value.businessName mustEqual anyName
    }

    "must build when business name has been answered" in {
      val answers = anEmptyAnswer.set(BusinessNamePage, anyName).success.value
      val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers)
      viewModel.value.businessName mustEqual anyName
    }

    "must contain the expected rows (minimal set)" in {
      val answers = anEmptyAnswer.set(BusinessNamePage, anyName).success.value
      val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers)
      viewModel.value.primaryContactList.rows mustBe empty
      viewModel.value.secondaryContactList.rows mustBe empty
    }

    "must contain the expected rows (maximal set)" in {
      val answers =
        anEmptyAnswer
          .set(BusinessNamePage, anyName).success.value
          .set(PrimaryContactNamePage, "name").success.value
          .set(PrimaryContactEmailAddressPage, "email").success.value
          .set(CanPhonePrimaryContactPage, true).success.value
          .set(PrimaryContactPhoneNumberPage, "phone").success.value
          .set(HasSecondaryContactPage, true).success.value
          .set(SecondaryContactNamePage, "name").success.value
          .set(SecondaryContactEmailAddressPage, "email").success.value
          .set(CanPhoneSecondaryContactPage, true).success.value
          .set(SecondaryContactPhoneNumberPage, "phone").success.value
        
      val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers)
      viewModel.value.primaryContactList.rows.size mustEqual 4
      viewModel.value.secondaryContactList.rows.size mustEqual 5
    }

    "must not build when business name has not been answered" - {

      "and the registration response is a match with Id not containing an organisation name" in {
        val answers = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithId("safe", anyAddress, None)))
        val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers)
        viewModel must not be defined
      }

      "and the registration response is a match without Id" in {
        val answers = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithoutId("safe")))
        val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers)
        viewModel must not be defined
      }

      "and the registration response is not a match" in {
        val answers = anEmptyAnswer.copy(registrationResponse = Some(NoMatchResponse()))
        val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers)
        viewModel must not be defined
      }

      "and a registration response is not available" in {
        val viewModel = CheckYourAnswersOrganisationViewModel.apply(anEmptyAnswer)
        viewModel must not be defined
      }
    }
  }
}
