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

import builders.UserAnswersBuilder.aUserAnswers
import forms.PrimaryContactPhoneNumberFormProvider
import models.NormalMode
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.PrimaryContactPhoneNumberPage

class PrimaryContactPhoneNumberViewModelSpec extends AnyFreeSpec with Matchers {

  private val anyMode = NormalMode
  private val anyName = "name"
  private val formProvider = new PrimaryContactPhoneNumberFormProvider()

  private val underTest = PrimaryContactPhoneNumberViewModel

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when PrimaryContactPhoneNumberPage answer available" in {
      val form = formProvider(anyName)
      val anyString = "some-string"
      val userAnswers = aUserAnswers.set(PrimaryContactPhoneNumberPage, anyString).get

      underTest.apply(anyMode, userAnswers, form, anyName) mustBe
        PrimaryContactPhoneNumberViewModel(mode = anyMode, form = form.fill(anyString), anyName)
    }

    "must return ViewModel without pre-filled form when PrimaryContactPhoneNumberPage answer not available" in {
      val emptyForm = formProvider(anyName)
      val userAnswers = aUserAnswers.remove(PrimaryContactPhoneNumberPage).get

      underTest.apply(anyMode, userAnswers, emptyForm, anyName) mustBe
        PrimaryContactPhoneNumberViewModel(mode = anyMode, form = emptyForm, anyName)
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider(anyName).bind(Map(PrimaryContactPhoneNumberPage.toString -> "unknown-value"))
      val userAnswers = aUserAnswers.remove(PrimaryContactPhoneNumberPage).get

      underTest.apply(anyMode, userAnswers, formWithErrors, anyName) mustBe
        PrimaryContactPhoneNumberViewModel(mode = anyMode, form = formWithErrors, anyName)
    }
  }
}
