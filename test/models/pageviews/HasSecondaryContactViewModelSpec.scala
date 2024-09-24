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
import forms.HasSecondaryContactFormProvider
import models.NormalMode
import pages.HasSecondaryContactPage

class HasSecondaryContactViewModelSpec extends SpecBase {

  private val anyMode = NormalMode
  private val anyName = "name"
  private val formProvider = new HasSecondaryContactFormProvider()

  private val underTest = HasSecondaryContactViewModel

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when HasSecondaryContactPage answer available" in {
      val form = formProvider(anyName)
      val anyBoolean = true
      val userAnswers = aUserAnswers.set(HasSecondaryContactPage, anyBoolean).get

      underTest.apply(anyMode, userAnswers, form, anyName) mustBe
        HasSecondaryContactViewModel(mode = anyMode, form = form.fill(anyBoolean), anyName)
    }

    "must return ViewModel without pre-filled form when HasSecondaryContactPage answer not available" in {
      val emptyForm = formProvider(anyName)
      val userAnswers = aUserAnswers.remove(HasSecondaryContactPage).get

      underTest.apply(anyMode, userAnswers, emptyForm, anyName) mustBe
        HasSecondaryContactViewModel(mode = anyMode, form = emptyForm, anyName)
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider(anyName).bind(Map(HasSecondaryContactPage.toString -> "unknown-value"))
      val userAnswers = aUserAnswers.remove(HasSecondaryContactPage).get

      underTest.apply(anyMode, userAnswers, formWithErrors, anyName) mustBe
        HasSecondaryContactViewModel(mode = anyMode, form = formWithErrors, anyName)
    }
  }
}
