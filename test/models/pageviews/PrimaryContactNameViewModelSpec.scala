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
import forms.PrimaryContactNameFormProvider
import models.NormalMode
import pages.PrimaryContactNamePage

class PrimaryContactNameViewModelSpec extends SpecBase {

  private val anyMode = NormalMode
  private val formProvider = new PrimaryContactNameFormProvider()

  private val underTest = PrimaryContactNameViewModel

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when PrimaryContactNamePage answer available" in {
      val form = formProvider()
      val anyString = "some-string"
      val userAnswers = aUserAnswers.set(PrimaryContactNamePage, anyString).get

      underTest.apply(anyMode, userAnswers, form, true) mustBe
        PrimaryContactNameViewModel(mode = anyMode, form = form.fill(anyString), true)
    }

    "must return ViewModel without pre-filled form when PrimaryContactNamePage answer not available" in {
      val emptyForm = formProvider()
      val userAnswers = aUserAnswers.remove(PrimaryContactNamePage).get

      underTest.apply(anyMode, userAnswers, emptyForm, false) mustBe
        PrimaryContactNameViewModel(mode = anyMode, form = emptyForm, false)
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider().bind(Map(PrimaryContactNamePage.toString -> "unknown-value"))
      val userAnswers = aUserAnswers.remove(PrimaryContactNamePage).get

      underTest.apply(anyMode, userAnswers, formWithErrors, true) mustBe
        PrimaryContactNameViewModel(mode = anyMode, form = formWithErrors, true)
    }
  }
}
