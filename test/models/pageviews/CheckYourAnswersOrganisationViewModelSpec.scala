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

import base.SpecBase
import builders.AddressBuilder.anyAddress
import builders.BusinessAddressBuilder.anyBusinessAddress
import builders.UserAnswersBuilder.{aUserAnswers, anEmptyAnswer}
import models.registration.responses.{MatchResponseWithId, MatchResponseWithoutId, NoMatchResponse}
import org.scalatest.{OptionValues, TryValues}
import pages.*
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class CheckYourAnswersOrganisationViewModelSpec extends SpecBase with OptionValues with TryValues {

  private implicit val messages: Messages = stubMessages()

  ".apply" - {
    "must build when the registration response is a match with Id containing an organisation name" in {
      val answers = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithId("safe", anyAddress, Some("any-name"))))
      val underTest = CheckYourAnswersOrganisationViewModel.apply(answers)
      underTest.value.businessName mustEqual "any-name"
    }

    "must build when mo match with id and business name has been answered" in {
      val answers = anEmptyAnswer.set(BusinessNameNoUtrPage, "any-name").success.value
      val underTest = CheckYourAnswersOrganisationViewModel.apply(answers)
      underTest.value.businessName mustEqual "any-name"
    }

    "must contain the expected rows (minimal set)" in {
      val answers = aUserAnswers
      val underTest = CheckYourAnswersOrganisationViewModel.apply(answers)
      underTest.value.primaryContact.rows mustBe empty
      underTest.value.secondaryContact.rows mustBe empty
      underTest.value.businessDetails mustBe empty
    }

    "must contain the expected rows (maximal set)" in {
      val answers = anEmptyAnswer
        .set(BusinessNameNoUtrPage, "any-name").success.value
        .set(HasBusinessTradingNamePage, true).success.value
        .set(BusinessEnterTradingNamePage, "any-trading-name").success.value
        .set(BusinessAddressPage, anyBusinessAddress).success.value
        .set(PrimaryContactNamePage, "name").success.value
        .set(PrimaryContactEmailAddressPage, "email").success.value
        .set(CanPhonePrimaryContactPage, true).success.value
        .set(PrimaryContactPhoneNumberPage, "phone").success.value
        .set(HasSecondaryContactPage, true).success.value
        .set(SecondaryContactNamePage, "name").success.value
        .set(SecondaryContactEmailAddressPage, "email").success.value
        .set(CanPhoneSecondaryContactPage, true).success.value
        .set(SecondaryContactPhoneNumberPage, "phone").success.value

      val underTest = CheckYourAnswersOrganisationViewModel.apply(answers)
      underTest.value.primaryContact.rows.size mustEqual 4
      underTest.value.secondaryContact.rows.size mustEqual 5
      underTest.value.businessDetails.get.rows.size mustEqual 4
    }

    "must not build when business name has not been answered" - {
      "and the registration response is a match with Id not containing an organisation name" in {
        val answers = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithId("safe", anyAddress, None)))
        val underTest = CheckYourAnswersOrganisationViewModel.apply(answers)
        underTest must not be defined
      }

      "and the registration response is a match without Id" in {
        val answers = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithoutId("safe")))
        val underTest = CheckYourAnswersOrganisationViewModel.apply(answers)
        underTest must not be defined
      }

      "and the registration response is not a match" in {
        val answers = anEmptyAnswer.copy(registrationResponse = Some(NoMatchResponse()))
        val underTest = CheckYourAnswersOrganisationViewModel.apply(answers)
        underTest must not be defined
      }

      "and a registration response is not available" in {
        val underTest = CheckYourAnswersOrganisationViewModel.apply(anEmptyAnswer)
        underTest must not be defined
      }
    }
  }
}
