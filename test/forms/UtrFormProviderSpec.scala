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

import forms.UtrFormProvider.{UtrFormKey, UtrRegex}
import forms.behaviours.StringFieldBehaviours
import org.scalatest.matchers.should.Matchers.should
import play.api.data.FormError

class UtrFormProviderSpec extends StringFieldBehaviours {

  private val base = "utr"

  private val requiredKey = s"$base.error.required"
  private val formatKey = s"$base.error.format"

  private val underTest = new UtrFormProvider()(base)

  "UtrFormProvider must provide a form" - {
    "which passes validation for a string as UTR number when" - {
      "string has 10 numbers" in {
        val formInput = Map(UtrFormKey -> "1234567890")
        underTest.bind(formInput).value mustBe Some("1234567890")
      }

      "string has 10 numbers and 'k' with spaces" in {
        val formInput = Map(UtrFormKey -> "k 12 34 5678 90 ")
        val value1 = underTest.bind(formInput)
        value1.value mustBe Some("k1234567890")
      }

      "string has 13 digits" in {
        val formInput = Map(UtrFormKey -> "1234567890123")
        underTest.bind(formInput).value mustBe Some("1234567890123")
      }

      "when string has 13 numbers and 'K' with spaces" in {
        val formInput = Map(UtrFormKey -> "K 12 34 5678 90 123 ")
        val value1 = underTest.bind(formInput)
        value1.value mustBe Some("K1234567890123")
      }
    }

    "which fails validation for a string when" - {
      "is empty string" in {
        val formInput = Map(UtrFormKey -> "")
        underTest.bind(formInput).errors mustBe Seq(FormError(UtrFormKey, requiredKey))
      }

      "string has numbers different than 10 or 13" in {
        val formInput = Map(UtrFormKey -> "123456789012")
        underTest.bind(formInput).errors mustBe Seq(FormError(UtrFormKey, formatKey, Seq(UtrRegex)))
      }
    }
  }
}
