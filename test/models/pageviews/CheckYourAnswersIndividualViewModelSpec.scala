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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.*
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class CheckYourAnswersIndividualViewModelSpec extends AnyFreeSpec with Matchers with OptionValues with TryValues {

  private implicit val messages: Messages = stubMessages()

  ".apply" - {

    "must contain the expected rows (minimal set)" in {
      val viewModel = CheckYourAnswersIndividualViewModel.apply(anEmptyAnswer)
      viewModel.list.rows mustBe empty
    }

    "must contain the expected rows (maximal set)" in {
      val answers =
        anEmptyAnswer
          .set(IndividualEmailAddressPage, "email").success.value
          .set(CanPhoneIndividualPage, true).success.value
          .set(IndividualPhoneNumberPage, "phone").success.value

      val viewModel = CheckYourAnswersIndividualViewModel.apply(answers)
      viewModel.list.rows.size mustEqual 3
    }
  }
}
