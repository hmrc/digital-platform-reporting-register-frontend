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
import play.api.data.FormError

class PrimaryContactNameFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "primaryContactName.error.required"
  private val formatKey = "primaryContactName.error.format"
  private val lengthKey = "primaryContactName.error.length"
  private val maxLength = 105

  private val underTest = new PrimaryContactNameFormProvider()()

  ".value" - {
    val fieldName = "value"

    behave like fieldThatBindsValidData(
      underTest,
      fieldName,
      safeNameInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      underTest,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, formatKey, Seq(Validation.nameInputPattern.toString))
    )
    
    behave like fieldWithMaxLength(
      underTest,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      underTest,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
