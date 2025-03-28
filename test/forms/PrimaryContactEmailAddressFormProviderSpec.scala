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
import org.scalacheck.Gen
import play.api.data.FormError

class PrimaryContactEmailAddressFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "primaryContactEmailAddress.error.required"
  private val lengthKey = "primaryContactEmailAddress.error.length"
  private val formatKey = "primaryContactEmailAddress.error.format"
  private val maxLength = 132
  private val anyName = "name"
  
  private val basicEmail = Gen.const("foo@example.com")
  private val emailWithSpecialChars = Gen.const("!#$%&'*-+/=?^_`{}~123@foo-bar.example.com")
  private val validData = Gen.oneOf(basicEmail, emailWithSpecialChars)

  private val underTest = new PrimaryContactEmailAddressFormProvider()(anyName)

  ".value" - {
    val fieldName = "value"

    behave like fieldThatBindsValidData(
      underTest,
      fieldName,
      validData
    )

    behave like fieldWithMaxLength(
      underTest,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, anyName))
    )

    behave like mandatoryField(
      underTest,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(anyName))
    )

    "not allow invalid email addreses" in {

      val noAt = "fooexample.com"
      val noUserName = "@example.com"
      val noDomain = "foo@example"
      val commaName = "foo,example@example.com"
      val invalidData = Gen.oneOf(noAt, noUserName, noDomain, commaName).sample.value

      val result = underTest.bind(Map("value" -> invalidData)).apply(fieldName)
      result.errors mustEqual Seq(FormError(fieldName, formatKey, Seq(Validation.emailPattern.toString)))
    }
  }
}
