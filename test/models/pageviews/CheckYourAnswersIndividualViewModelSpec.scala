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
import builders.InternationalAddressBuilder.anInternationalAddress
import builders.UserAnswersBuilder.*
import models.IndividualName
import org.scalatest.{OptionValues, TryValues}
import pages.*
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class CheckYourAnswersIndividualViewModelSpec extends SpecBase with OptionValues with TryValues {

  private implicit val messages: Messages = stubMessages()

  ".apply" - {
    "must contain the expected rows (minimal set)" in {
      val viewModel = CheckYourAnswersIndividualViewModel.apply(anEmptyAnswer)
      viewModel.contactDetails.rows mustBe empty
    }

    "must contain the expected rows (maximal set)" in {
      val answers = aUserAnswers
        .set(IndividualEmailAddressPage, "email").success.value
        .set(CanPhoneIndividualPage, true).success.value
        .set(IndividualPhoneNumberPage, "phone").success.value
      val viewModel = CheckYourAnswersIndividualViewModel.apply(answers)

      viewModel.contactDetails.rows.size mustEqual 3
      viewModel.yourDetails mustBe None
    }

    "must contain expected rows when registrationResponse is not present" in {
      val answers = aUserAnswers.copy(registrationResponse = None)
        .set(IndividualEmailAddressPage, "email").success.value
        .set(CanPhoneIndividualPage, true).success.value
        .set(IndividualPhoneNumberPage, "phone").success.value
        .set(IndividualNamePage, IndividualName("Homer", "Simpson")).success.value
        .set(DateOfBirthPage, LocalDate.of(2000, 1, 1)).success.value
        .set(AddressInUkPage, true).success.value
        .set(InternationalAddressPage, anInternationalAddress).success.value
      val viewModel = CheckYourAnswersIndividualViewModel.apply(answers)

      viewModel.contactDetails.rows.size mustEqual 3
      viewModel.yourDetails.get.rows.size mustEqual 4
    }
  }
}
