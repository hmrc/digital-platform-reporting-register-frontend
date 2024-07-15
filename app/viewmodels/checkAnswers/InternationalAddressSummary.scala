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
import pages.InternationalAddressPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object InternationalAddressSummary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(InternationalAddressPage).map {
      answer =>
        val line2 = answer.line2.map(x => HtmlFormat.escape(x).toString + "<br/>").getOrElse("")
        val region = answer.region.map(x => HtmlFormat.escape(x).toString + "<br/>").getOrElse("")
        val postal = answer.postal.map(x => HtmlFormat.escape(x).toString + "<br/>").getOrElse("")


        val value = HtmlFormat.escape(answer.line1).toString + "<br/>" +
                  line2 +
                  HtmlFormat.escape(answer.city).toString + "<br/>" +
                  region +
                  postal +
                  HtmlFormat.escape(answer.country.name).toString

        SummaryListRowViewModel(
          key     = "internationalAddress.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.InternationalAddressController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("internationalAddress.change.hidden"))
          )
        )
    }
}
