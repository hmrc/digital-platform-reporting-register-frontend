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
import builders.UserAnswersBuilder.aUserAnswers
import forms.IndividualPhoneNumberFormProvider
import models.NormalMode
import pages.IndividualPhoneNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class IndividualPhoneNumberViewModelSpec extends SpecBase {

  private implicit val msgs: Messages = stubMessages()
  private val anyMode = NormalMode
  private val formProvider = new IndividualPhoneNumberFormProvider()

  private val underTest = IndividualPhoneNumberViewModel

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when IndividualPhoneNumberPage answer available" in {
      val form = formProvider()
      val anyString = "some-string"
      val userAnswers = aUserAnswers.set(IndividualPhoneNumberPage, anyString).get

      underTest.apply(anyMode, userAnswers, form) mustBe
        IndividualPhoneNumberViewModel(mode = anyMode, form = form.fill(anyString))
    }

    "must return ViewModel without pre-filled form when IndividualPhoneNumberPage answer not available" in {
      val emptyForm = formProvider()
      val userAnswers = aUserAnswers.remove(IndividualPhoneNumberPage).get

      underTest.apply(anyMode, userAnswers, emptyForm) mustBe
        IndividualPhoneNumberViewModel(mode = anyMode, form = emptyForm)
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider().bind(Map(IndividualPhoneNumberPage.toString -> "unknown-value"))
      val userAnswers = aUserAnswers.remove(IndividualPhoneNumberPage).get

      underTest.apply(anyMode, userAnswers, formWithErrors) mustBe
        IndividualPhoneNumberViewModel(mode = anyMode, form = formWithErrors)
    }
  }
}
