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
import forms.BusinessNameFormProvider
import models.BusinessType.*
import models.NormalMode
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.{BusinessNamePage, BusinessTypePage}

class BusinessNameViewModelSpec extends SpecBase {

  private val anyMode = NormalMode
  private val formProvider = new BusinessNameFormProvider()

  private val underTest = BusinessNameViewModel

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when BusinessNamePage answer available" in {
      val form = formProvider()
      val anyString = "some-string"
      val userAnswers = aUserAnswers.set(BusinessTypePage, LimitedCompany).success.value
        .set(BusinessNamePage, anyString).get

      underTest.apply(anyMode, userAnswers, form) mustBe BusinessNameViewModel(
        mode = anyMode,
        form = form.fill(anyString),
        businessType = LimitedCompany,
        showLimitedCompanyHint = true
      )
    }

    "must return ViewModel with pre-filled form when BusinessNamePage answer available LLP" in {
      val form = formProvider()
      val anyString = "some-string"
      val userAnswers = aUserAnswers.set(BusinessTypePage, Llp).success.value
        .set(BusinessNamePage, anyString).get

      underTest.apply(anyMode, userAnswers, form) mustBe BusinessNameViewModel(
        mode = anyMode,
        form = form.fill(anyString),
        businessType = LimitedCompany,
        showLimitedCompanyHint = true
      )
    }

    "must return ViewModel without pre-filled form when BusinessNamePage answer not available" in {
      val emptyForm = formProvider()
      val userAnswers = aUserAnswers.set(BusinessTypePage, Partnership).success.value
        .remove(BusinessNamePage).get

      underTest.apply(anyMode, userAnswers, emptyForm) mustBe BusinessNameViewModel(
        mode = anyMode,
        form = emptyForm,
        businessType = Partnership,
        showLimitedCompanyHint = false
      )
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider().bind(Map(BusinessNamePage.toString -> "unknown-value"))
      val userAnswers = aUserAnswers.set(BusinessTypePage, AssociationOrTrust).success.value
        .remove(BusinessNamePage).get

      underTest.apply(anyMode, userAnswers, formWithErrors) mustBe BusinessNameViewModel(
        mode = anyMode,
        form = formWithErrors,
        businessType = AssociationOrTrust,
        showLimitedCompanyHint = false
      )
    }
  }
}
