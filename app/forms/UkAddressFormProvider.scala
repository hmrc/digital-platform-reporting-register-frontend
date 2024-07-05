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

import forms.common.UkPostCode
import forms.mappings.Mappings
import models.UkAddress
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class UkAddressFormProvider @Inject() extends UkPostCode {

   def apply(): Form[UkAddress] = Form(
     mapping(
      "line1" -> text("ukAddress.error.line1.required")
        .verifying(maxLength(100, "ukAddress.error.line1.length")),
      "line2" -> optional(text("")
        .verifying(maxLength(100, "ukAddress.error.line2.length"))),
      "town" -> text("ukAddress.error.town.required")
        .verifying(maxLength(30, "ukAddress.error.town.length")),
      "county" -> optional(text("")
        .verifying(maxLength(30, "ukAddress.error.county.length"))),
      "postCode" -> ukPostCode("ukAddress.error.postCode.required", "ukAddress.error.postCode.length")
    )(UkAddress.apply)(x => Some((x.line1, x.line2, x.town, x.county, x.postCode)))
   )
 }
