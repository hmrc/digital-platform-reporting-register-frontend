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

package viewmodels.checkAnswers

import controllers.routes
import models.{CheckMode, UserAnswers}
import pages.BusinessAddressPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import scala.language.postfixOps


object BusinessAddressSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(BusinessAddressPage).map { answer =>
      val value = Seq(
        Some(HtmlFormat.escape(answer.addressLine1)),
        answer.addressLine2.map(HtmlFormat.escape),
        Some(HtmlFormat.escape(answer.city)),
        answer.region.map(HtmlFormat.escape),
        answer.postalCode.map(HtmlFormat.escape),
        Some(HtmlFormat.escape(answer.country.name))
      ).flatten.map(_.toString).mkString("<br/>")

      SummaryListRowViewModel(
        key = "businessAddress.checkYourAnswersLabel",
        value = ValueViewModel(HtmlContent(value)),
        actions = Seq(
          ActionItemViewModel("site.change", routes.BusinessAddressController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("businessAddress.change.hidden"))
        )
      )
    }
}