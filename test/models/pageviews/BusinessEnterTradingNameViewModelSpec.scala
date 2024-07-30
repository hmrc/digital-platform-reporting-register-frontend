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
import forms.BusinessEnterTradingNameFormProvider
import models.NormalMode
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.BusinessEnterTradingNamePage

class BusinessEnterTradingNameViewModelSpec extends AnyFreeSpec with Matchers {

  private val anyMode = NormalMode
  private val formProvider = new BusinessEnterTradingNameFormProvider()

  private val underTest = BusinessEnterTradingNameViewModel

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when BusinessEnterTradingNamePage answer available" in {
      val form = formProvider()
      val anyString = "some-string"
      val userAnswers = aUserAnswers.set(BusinessEnterTradingNamePage, anyString).get

      underTest.apply(anyMode, userAnswers, form) mustBe
        BusinessEnterTradingNameViewModel(mode = anyMode, form = form.fill(anyString))
    }

    "must return ViewModel without pre-filled form when BusinessEnterTradingNamePage answer not available" in {
      val emptyForm = formProvider()
      val userAnswers = aUserAnswers.remove(BusinessEnterTradingNamePage).get

      underTest.apply(anyMode, userAnswers, emptyForm) mustBe
        BusinessEnterTradingNameViewModel(mode = anyMode, form = emptyForm)
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider().bind(Map(BusinessEnterTradingNamePage.toString -> "unknown-value"))
      val userAnswers = aUserAnswers.remove(BusinessEnterTradingNamePage).get

      underTest.apply(anyMode, userAnswers, formWithErrors) mustBe
        BusinessEnterTradingNameViewModel(mode = anyMode, form = formWithErrors)
    }
  }
}
