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

import forms.behaviours.OptionFieldBehaviours
import models.BusinessType.*
import models.RegistrationType.*
import models.{BusinessType, RegistrationType}
import play.api.data.FormError

class BusinessTypeFormProviderSpec extends OptionFieldBehaviours {

  val formProvider = new BusinessTypeFormProvider()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "businessType.error.required"

    "for a Platform Operator" - {

      val form = formProvider(PlatformOperator)
      val validOptions = Seq(LimitedCompany, Partnership, Llp, AssociationOrTrust)

      "must bind valid options" in {

        for (value <- validOptions) {

          val result = form.bind(Map(fieldName -> value.toString)).apply(fieldName)
          result.value.value mustEqual value.toString
          result.errors mustBe empty
        }
      }

      "must not bind options that only apply to Third Parties" in {

        for(value <- Seq(SoleTrader, Individual)) {

          val result = form.bind(Map(fieldName -> value.toString)).apply(fieldName)
          result.errors must contain only FormError(fieldName, requiredKey)
        }
      }

      "must not bind invalid options" in {

        val generator = stringsExceptSpecificValues(validOptions.map(_.toString))

        forAll(generator -> "invalidValue") {
          value =>

            val result = form.bind(Map(fieldName -> value)).apply(fieldName)
            result.errors must contain only FormError(fieldName, "error.invalid")
        }
      }
    }

    "for a Third Party" - {

      val form = formProvider(ThirdParty)
      val validOptions = Seq(LimitedCompany, Partnership, Llp, AssociationOrTrust, SoleTrader, Individual)

      "must bind valid options" in {

        for (value <- validOptions) {

          val result = form.bind(Map(fieldName -> value.toString)).apply(fieldName)
          result.value.value mustEqual value.toString
          result.errors mustBe empty
        }
      }

      "must not bind invalid options" in {

        val generator = stringsExceptSpecificValues(validOptions.map(_.toString))

        forAll(generator -> "invalidValue") {
          value =>

            val result = form.bind(Map(fieldName -> value)).apply(fieldName)
            result.errors must contain only FormError(fieldName, "error.invalid")
        }
      }
    }

    behave like mandatoryField(
      formProvider(RegistrationType.PlatformOperator),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
