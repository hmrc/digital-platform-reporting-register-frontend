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

import models.registration.Address
import models.{CountriesList, Country, InternationalAddress, UkAddress}
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem
import viewmodels.govuk.all.{FluentSelectItem, SelectItemViewModel}

object ViewUtils {

  def title(form: Form[?], title: String, section: Option[String] = None)(implicit messages: Messages): String =
    titleNoForm(
      title = s"${errorPrefix(form)} ${messages(title)}",
      section = section
    )

  def titleNoForm(title: String, section: Option[String] = None)(implicit messages: Messages): String =
    s"${messages(title)} - ${section.fold("")(messages(_) + " - ")}${messages("service.name")} - ${messages("site.govuk")}"

  private def errorPrefix(form: Form[?])(implicit messages: Messages): String = {
    if (form.hasErrors || form.hasGlobalErrors) messages("error.title.prefix") else ""
  }

  def formatUkAddress(ukAddress: UkAddress, countriesList: CountriesList): String =
    formatAddress(Address(ukAddress), countriesList)

  def formatInternationalAddress(internationalAddress: InternationalAddress, countriesList: CountriesList): String =
    formatAddress(Address(internationalAddress), countriesList)

  def formatAddress(address: Address, countriesList: CountriesList): String = {
    val code = address.countryCode
    val lines = Seq(
      formatLine(address.addressLine1.trim),
      formatLine(address.addressLine2),
      formatLine(address.addressLine3),
      formatLine(address.addressLine4),
      formatLine(address.postalCode),
      formatLine(countriesList.allCountries.find(_.code == code).map(_.name).getOrElse(code))
    )

    lines.flatten.map(HtmlFormat.escape).mkString("<br/>")
  }

  def countrySelectItems(countries: Seq[Country]): Seq[SelectItem] = SelectItem(value = None, text = "") +:
    countries.map { country =>
      SelectItemViewModel(
        value = country.code,
        text = country.name
      ).withAttribute("aria-describedby", country.name)
    }

  private def formatLine(line: Option[String]): Option[String] =
    line.map(_.trim).filterNot(_.isEmpty)

  private def formatLine(line: String): Option[String] =
    formatLine(Some(line))
}
