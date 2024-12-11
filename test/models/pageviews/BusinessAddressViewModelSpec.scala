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
import forms.BusinessAddressFormProvider
import generators.Generators
import models.{DefaultCountriesList, NormalMode}
import pages.BusinessAddressPage

class BusinessAddressViewModelSpec extends SpecBase with Generators {

  private val anyMode = NormalMode
  private val countriesList = new DefaultCountriesList()
  private val formProvider = new BusinessAddressFormProvider(countriesList)

  private val underTest = BusinessAddressViewModel

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when BusinessAddressPage answer available" in {
      val form = formProvider()
      val businessAddress = arbitraryBusinessAddress.arbitrary.sample.get
      val userAnswers = aUserAnswers.set(BusinessAddressPage, businessAddress).get

      underTest.apply(anyMode, userAnswers, form) mustBe
        BusinessAddressViewModel(mode = anyMode, form = form.fill(businessAddress))
    }

    "must return ViewModel without pre-filled form when BusinessAddressPage answer not available" in {
      val emptyForm = formProvider()
      val userAnswers = aUserAnswers.remove(BusinessAddressPage).get

      underTest.apply(anyMode, userAnswers, emptyForm) mustBe
        BusinessAddressViewModel(mode = anyMode, form = emptyForm)
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider().bind(Map(BusinessAddressPage.toString -> "unknown-value"))
      val userAnswers = aUserAnswers.remove(BusinessAddressPage).get

      underTest.apply(anyMode, userAnswers, formWithErrors) mustBe
        BusinessAddressViewModel(mode = anyMode, form = formWithErrors)
    }
  }
}
