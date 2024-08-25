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

import forms.common.Validation

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.*
import models.SoleTraderName

class SoleTraderNameFormProvider @Inject() extends Mappings {

   def apply(): Form[SoleTraderName] = Form(
     mapping(
      "firstName" -> text("soleTraderName.error.firstName.required")
        .verifying(firstError(
          maxLength(35, "soleTraderName.error.firstName.length"),
          regexp(Validation.textInputPattern.toString, "soleTraderName.error.firstName.format")
        )),
      "lastName" -> text("soleTraderName.error.lastName.required")
        .verifying(firstError(
          maxLength(35, "soleTraderName.error.lastName.length"),
          regexp(Validation.textInputPattern.toString, "soleTraderName.error.lastName.format")
        ))
    )(SoleTraderName.apply)(x => Some((x.firstName, x.lastName)))
   )
 }
