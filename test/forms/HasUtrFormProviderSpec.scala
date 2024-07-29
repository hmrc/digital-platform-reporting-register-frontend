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
import models.BusinessType.{AssociationOrTrust, Individual, LimitedCompany, Llp, Partnership, SoleTrader}
import play.api.data.FormError

class HasUtrFormProviderSpec extends BooleanFieldBehaviours {

  private val fieldName = "value"

  private val requiredCorporationTaxKey = "hasUtrCorporationTax.error.required"
  private val requiredPartnershipKey = "hasUtrPartnership.error.required"
  private val requiredSelfAssessmentKey = "hasUtrSelfAssessment.error.required"
  private val invalidKey = "error.boolean"

  for (businessType <- Seq(LimitedCompany, AssociationOrTrust)) {
    s".value $businessType" - {
      val underTest = new HasUtrFormProvider()(businessType)

      behave like booleanField(
        underTest,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        underTest,
        fieldName,
        requiredError = FormError(fieldName, requiredCorporationTaxKey)
      )
    }
  }

  for (businessType <- Seq(Llp, Partnership)) {
    s".value $businessType" - {
      val underTest = new HasUtrFormProvider()(businessType)

      behave like booleanField(
        underTest,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        underTest,
        fieldName,
        requiredError = FormError(fieldName, requiredPartnershipKey)
      )
    }
  }

  for (businessType <- Seq(SoleTrader, Individual)) {
    s".value $businessType" - {
      val underTest = new HasUtrFormProvider()(businessType)

      behave like booleanField(
        underTest,
        fieldName,
        invalidError = FormError(fieldName, invalidKey)
      )

      behave like mandatoryField(
        underTest,
        fieldName,
        requiredError = FormError(fieldName, requiredSelfAssessmentKey)
      )
    }
  }
}
