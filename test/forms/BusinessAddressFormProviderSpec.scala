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

package forms

import forms.behaviours.StringFieldBehaviours
import forms.common.Validation
import models.Country
import org.scalacheck.Gen
import play.api.data.FormError

class BusinessAddressFormProviderSpec extends StringFieldBehaviours {

  private val form = new BusinessAddressFormProvider()()

  ".addressLine1" - {
    val fieldName = "addressLine1"
    val requiredKey = "businessAddress.error.addressLine1.required"
    val lengthKey = "businessAddress.error.addressLine1.length"
    val formatKey = "businessAddress.error.addressLine1.format"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeTextInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeTextInputsWithMaxLength(maxLength),
      FormError(fieldName, formatKey, Seq(Validation.textInputPattern.toString))
    )
    
    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".addressLine2" - {
    val fieldName = "addressLine2"
    val lengthKey = "businessAddress.error.addressLine2.length"
    val formatKey = "businessAddress.error.addressLine2.format"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeTextInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeTextInputsWithMaxLength(maxLength),
      FormError(fieldName, formatKey, Seq(Validation.textInputPattern.toString))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }

  ".city" - {
    val fieldName = "city"
    val requiredKey = "businessAddress.error.city.required"
    val lengthKey = "businessAddress.error.city.length"
    val formatKey = "businessAddress.error.city.format"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeTextInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeTextInputsWithMaxLength(maxLength),
      FormError(fieldName, formatKey, Seq(Validation.textInputPattern.toString))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".region" - {
    val fieldName = "region"
    val lengthKey = "businessAddress.error.region.length"
    val formatKey = "businessAddress.error.region.format"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeTextInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeTextInputsWithMaxLength(maxLength),
      FormError(fieldName, formatKey, Seq(Validation.textInputPattern.toString))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }

  ".postalCode" - {
    val fieldName = "postalCode"
    val requiredKey = "businessAddress.error.postalCode.required"
    val lengthKey = "businessAddress.error.postalCode.length"
    val formatKey = "businessAddress.error.postalCode.format"
    val maxLength = 10

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeTextInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeTextInputsWithMaxLength(maxLength),
      FormError(fieldName, formatKey, Seq(Validation.textInputPattern.toString))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
    
    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".country" - {
    val fieldName = "country"
    val requiredKey = "businessAddress.error.country.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(Country.nonUkInternationalCountries.map(_.code))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
