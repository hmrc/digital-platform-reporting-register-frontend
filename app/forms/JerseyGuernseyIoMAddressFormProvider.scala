/*
 * Copyright 2025 HM Revenue & Customs
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

import forms.common.{UkPostCode, Validation}
import models.{CountriesList, Country, JerseyGuernseyIoMAddress}
import play.api.data.Form
import play.api.data.Forms.*

import javax.inject.Inject

class JerseyGuernseyIoMAddressFormProvider @Inject()(countriesList: CountriesList) extends UkPostCode {
  private val maximumLength = 35

  def apply(): Form[JerseyGuernseyIoMAddress] = Form(
    mapping(
      "line1" -> text("jerseyGuernseyIoMAddress.error.line1.required")
        .verifying(firstError(
          maxLength(maximumLength, "jerseyGuernseyIoMAddress.error.line1.length"),
          regexp(Validation.textInputPattern.toString, "jerseyGuernseyIoMAddress.error.line1.format")
        )),
      "line2" -> optional(text("")
        .verifying(firstError(
          maxLength(maximumLength, "jerseyGuernseyIoMAddress.error.line2.length"),
          regexp(Validation.textInputPattern.toString, "jerseyGuernseyIoMAddress.error.line2.format")
        ))),
      "town" -> text("jerseyGuernseyIoMAddress.error.town.required")
        .verifying(firstError(
          maxLength(maximumLength, "jerseyGuernseyIoMAddress.error.town.length"),
          regexp(Validation.textInputPattern.toString, "jerseyGuernseyIoMAddress.error.town.format")
        )),
      "county" -> optional(text("")
        .verifying(firstError(
          maxLength(maximumLength, "jerseyGuernseyIoMAddress.error.county.length"),
          regexp(Validation.textInputPattern.toString, "jerseyGuernseyIoMAddress.error.county.format")
        ))),
      "postCode" -> text("jerseyGuernseyIoMAddress.error.postCode.required")
        .verifying(regexp(Validation.ukPostcodePattern.toString, "jerseyGuernseyIoMAddress.error.postCode.format")),
      "country" -> text("jerseyGuernseyIoMAddress.error.country.required")
        .verifying("jerseyGuernseyIoMAddress.error.country.required", value => countriesList.crownDependantCountries.exists(_.code == value))
        .transform[Country](value => countriesList.crownDependantCountries.find(_.code == value).get, _.code)
    )(JerseyGuernseyIoMAddress.apply)(x => Some((x.line1, x.line2, x.town, x.county, x.postCode, x.country)))
  )
}
