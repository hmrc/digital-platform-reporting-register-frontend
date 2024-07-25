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

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class CanPhonePrimaryContactFormProviderSpec extends BooleanFieldBehaviours {

  private val requiredKey = "canPhonePrimaryContact.error.required"
  private val invalidKey = "error.boolean"
  private val anyName = "name"

  private val underTest = new CanPhonePrimaryContactFormProvider()(anyName)

  ".value" - {
    val fieldName = "value"

    behave like booleanField(
      underTest,
      fieldName,
      invalidError = FormError(fieldName, invalidKey, Seq(anyName))
    )

    behave like mandatoryField(
      underTest,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(anyName))
    )
  }
}