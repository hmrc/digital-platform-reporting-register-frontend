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

import forms.mappings.Mappings
import models.BusinessType
import models.BusinessType.{AssociationOrTrust, Individual, LimitedCompany, Llp, Partnership, SoleTrader}
import play.api.data.Form

import javax.inject.Inject

class HasUtrFormProvider @Inject() extends Mappings {

  def apply(businessType: BusinessType): Form[Boolean] = businessType match {
    case LimitedCompany | AssociationOrTrust => Form("value" -> boolean("hasUtrCorporationTax.error.required"))
    case Llp | Partnership => Form("value" -> boolean("hasUtrPartnership.error.required"))
    case SoleTrader | Individual => Form("value" -> boolean("hasUtrSelfAssessment.error.required"))
  }
}
