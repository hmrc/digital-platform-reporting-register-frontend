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
import forms.SecondaryContactPhoneNumberFormProvider
import models.NormalMode
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.SecondaryContactPhoneNumberPage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class SecondaryContactPhoneNumberViewModelSpec extends AnyFreeSpec with Matchers {

  private implicit val msgs: Messages = stubMessages()
  private val anyName = "name"
  private val anyMode = NormalMode
  private val formProvider = new SecondaryContactPhoneNumberFormProvider()

  private val underTest = SecondaryContactPhoneNumberViewModel

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when SecondaryContactPhoneNumberPage answer available" in {
      val form = formProvider(anyName)
      val anyString = "some-string"
      val userAnswers = aUserAnswers.set(SecondaryContactPhoneNumberPage, anyString).get

      underTest.apply(anyMode, userAnswers, form, anyName) mustBe
        SecondaryContactPhoneNumberViewModel(mode = anyMode, form = form.fill(anyString), anyName)
    }

    "must return ViewModel without pre-filled form when SecondaryContactPhoneNumberPage answer not available" in {
      val emptyForm = formProvider(anyName)
      val userAnswers = aUserAnswers.remove(SecondaryContactPhoneNumberPage).get

      underTest.apply(anyMode, userAnswers, emptyForm, anyName) mustBe
        SecondaryContactPhoneNumberViewModel(mode = anyMode, form = emptyForm, anyName)
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider(anyName).bind(Map(SecondaryContactPhoneNumberPage.toString -> "unknown-value"))
      val userAnswers = aUserAnswers.remove(SecondaryContactPhoneNumberPage).get

      underTest.apply(anyMode, userAnswers, formWithErrors, anyName) mustBe
        SecondaryContactPhoneNumberViewModel(mode = anyMode, form = formWithErrors, anyName)
    }
  }
}
