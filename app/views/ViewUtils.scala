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

package views

import models.Country
import models.registration.Address
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat

object ViewUtils {

  def title(form: Form[_], title: String, section: Option[String] = None)(implicit messages: Messages): String =
    titleNoForm(
      title   = s"${errorPrefix(form)} ${messages(title)}",
      section = section
    )

  def titleNoForm(title: String, section: Option[String] = None)(implicit messages: Messages): String =
    s"${messages(title)} - ${section.fold("")(messages(_) + " - ")}${messages("service.name")} - ${messages("site.govuk")}"

  def errorPrefix(form: Form[_])(implicit messages: Messages): String = {
    if (form.hasErrors || form.hasGlobalErrors) messages("error.title.prefix") else ""
  }

  def formatAddress(address: Address): String = {
    val code = address.countryCode
    HtmlFormat.escape(address.addressLine1).toString + "<br/>" +
      address.addressLine2.map(HtmlFormat.escape(_).toString + "<br/>").getOrElse("") +
      address.addressLine3.map(HtmlFormat.escape(_).toString + "<br/>").getOrElse("") +
      address.addressLine4.map(HtmlFormat.escape(_).toString + "<br/>").getOrElse("") +
      address.postalCode.map(HtmlFormat.escape(_).toString + "<br/>").getOrElse("") +
      Country.allCountries.find(_.code == code).map(_.name).getOrElse(code)
  }
}
