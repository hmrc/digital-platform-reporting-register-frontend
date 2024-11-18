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
import models.BusinessType
import models.RegistrationType.PlatformOperator
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError

class ClaimEnrolmentFormProviderSpec extends StringFieldBehaviours {

  private val form = new ClaimEnrolmentFormProvider()()

  ".utr" - {
    val fieldName = "utr"
    val requiredKey = "claimEnrolment.error.utr.required"
    val formatKey = "claimEnrolment.error.utr.format"

    val utrGen = Gen.oneOf(
      Gen.listOfN(10, Gen.numChar).map(_.mkString),
      Gen.listOfN(13, Gen.numChar).map(_.mkString)
    )
    
    val invalidGen = Gen.oneOf(
      arbitrary[String].suchThat(_.trim.nonEmpty).suchThat(_.forall(_.isDigit) == false),
      Gen.numStr.suchThat(x => x.length != 10 && x.length != 13)
    )
    
    behave like fieldThatBindsValidData(
      form,
      fieldName,
      utrGen
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      invalidGen,
      FormError(fieldName, formatKey, Seq(Validation.utrPattern.toString))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".businessName" - {
    val fieldName = "businessName"
    val requiredKey = "claimEnrolment.error.businessName.required"
    val lengthKey = "claimEnrolment.error.businessName.length"
    val formatKey = "claimEnrolment.error.businessName.format"
    val maxLength = 105

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
  
  ".businessType" - {
    val fieldName = "businessType"
    val requiredKey = "claimEnrolment.error.businessType.required"
    val validValues = BusinessType.valuesForRegistrationType(PlatformOperator).map(_.toString)
    val invalidGen = arbitrary[String].suchThat(_.trim.nonEmpty).suchThat(x => !validValues.contains(x))

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(validValues)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      invalidGen,
      FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
