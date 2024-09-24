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
import forms.SecondaryContactEmailAddressFormProvider
import models.NormalMode
import pages.SecondaryContactEmailAddressPage

class SecondaryContactEmailAddressViewModelSpec extends SpecBase {

  private val anyName = "name"
  private val anyMode = NormalMode
  private val formProvider = new SecondaryContactEmailAddressFormProvider()

  private val underTest = SecondaryContactEmailAddressViewModel

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when SecondaryContactEmailAddressPage answer available" in {
      val form = formProvider(anyName)
      val anyString = "some-string"
      val userAnswers = aUserAnswers.set(SecondaryContactEmailAddressPage, anyString).get

      underTest.apply(anyMode, userAnswers, form, anyName) mustBe
        SecondaryContactEmailAddressViewModel(mode = anyMode, form = form.fill(anyString), anyName)
    }

    "must return ViewModel without pre-filled form when SecondaryContactEmailAddressPage answer not available" in {
      val emptyForm = formProvider(anyName)
      val userAnswers = aUserAnswers.remove(SecondaryContactEmailAddressPage).get

      underTest.apply(anyMode, userAnswers, emptyForm, anyName) mustBe
        SecondaryContactEmailAddressViewModel(mode = anyMode, form = emptyForm, anyName)
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider(anyName).bind(Map(SecondaryContactEmailAddressPage.toString -> "unknown-value"))
      val userAnswers = aUserAnswers.remove(SecondaryContactEmailAddressPage).get

      underTest.apply(anyMode, userAnswers, formWithErrors, anyName) mustBe
        SecondaryContactEmailAddressViewModel(mode = anyMode, form = formWithErrors, anyName)
    }
  }
}
