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
import forms.mappings.Mappings
import models.{BusinessAddress, Country}
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class BusinessAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[BusinessAddress] = Form(
    mapping(
      "addressLine1" -> text("businessAddress.error.addressLine1.required")
        .verifying(firstError(
          maxLength(35, "businessAddress.error.addressLine1.length"),
          regexp(Validation.textInputPattern.toString, "businessAddress.error.addressLine1.format")
        )),
      "addressLine2" -> optional(text("")
        .verifying(firstError(
          maxLength(35, "businessAddress.error.addressLine2.length"),
          regexp(Validation.textInputPattern.toString, "businessAddress.error.addressLine2.format")
        ))),
      "city" -> text("businessAddress.error.city.required")
        .verifying(firstError(
          maxLength(35, "businessAddress.error.city.length"),
          regexp(Validation.textInputPattern.toString, "businessAddress.error.city.format")
        )),
      "region" -> optional(text("")
        .verifying(firstError(
          maxLength(35, "businessAddress.error.region.length"),
          regexp(Validation.textInputPattern.toString, "businessAddress.error.region.format")
        ))),
      "postalCode" -> text("businessAddress.error.postalCode.required")
        .verifying(firstError(
          maxLength(10, "businessAddress.error.postalCode.length"),
          regexp(Validation.textInputPattern.toString, "businessAddress.error.postalCode.format")
        )),
      "country" -> text("businessAddress.error.country.required")
        .verifying("businessAddress.error.country.required", value => Country.nonUkInternationalCountries.exists(_.code == value))
        .transform[Country](value => Country.nonUkInternationalCountries.find(_.code == value).get, _.code)
    )(BusinessAddress.apply)(x => Some((x.addressLine1, x.addressLine2, x.city, x.region, x.postalCode, x.country)))
  )
}
