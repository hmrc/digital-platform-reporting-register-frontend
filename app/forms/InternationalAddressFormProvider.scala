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

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.InternationalAddress

class InternationalAddressFormProvider @Inject() extends Mappings {

   def apply(): Form[InternationalAddress] = Form(
     mapping(
      "line1" -> text("internationalAddress.error.line1.required")
        .verifying(maxLength(100, "internationalAddress.error.line1.length")),
      "line2" -> optional(text("")
        .verifying(maxLength(100, "internationalAddress.error.line2.length"))),
      "city" -> text("internationalAddress.error.city.required")
        .verifying(maxLength(100, "internationalAddress.error.city.length")),
       "region" -> optional(text("")
         .verifying(maxLength(100, "internationalAddress.error.region.length"))),
       "postal" -> optional(text("")
         .verifying(maxLength(100, "internationalAddress.error.postal.length"))),
       "country" -> text("internationalAddress.error.country.required")
         .verifying(maxLength(100, "internationalAddress.error.country.length"))
    )(InternationalAddress.apply)(x => Some((x.line1, x.line2, x.city, x.region, x.postal, x.country)))
   )
 }
