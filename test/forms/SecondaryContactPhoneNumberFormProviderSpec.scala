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
import org.scalacheck.Gen
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class SecondaryContactPhoneNumberFormProviderSpec extends StringFieldBehaviours {

  private implicit val msgs: Messages = stubMessages()
  private val requiredKey = "secondaryContactPhoneNumber.error.required"
  private val formatKey = "secondaryContactPhoneNumber.error.format"
  private val anyName = "name"

  private val underTest = new SecondaryContactPhoneNumberFormProvider()(anyName)

  ".value" - {
    val fieldName = "value"

    behave like fieldThatBindsValidData(
      underTest,
      fieldName,
      Gen.oneOf("07777777777", "+1 (555) 000 0000", "07777 777777   ")
    )

    behave like mandatoryField(
      underTest,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(anyName))
    )

    "must fail to bind an invalid phone number" in {
      underTest.bind(Map(fieldName -> "invalid")).error("value").value.message mustEqual formatKey
    }
  }
}
