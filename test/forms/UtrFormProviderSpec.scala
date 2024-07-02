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
import play.api.data.FormError

class UtrFormProviderSpec extends StringFieldBehaviours {

  val base = "utr"
  
  val requiredKey = s"$base.error.required"
  val formatKey = s"$base.error.format"
  val minLength = 10
  val maxLength = 13

  val form = new UtrFormProvider()(base)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMinMaxLength(minLength, maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, formatKey, Seq(maxLength))
    )

    behave like fieldWithMinLength(
      form,
      fieldName,
      minLength = minLength,
      lengthError = FormError(fieldName, formatKey, Seq(minLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
