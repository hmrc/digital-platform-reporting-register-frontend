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
import pages.DateOfBirthPage
import play.api.i18n.{Lang, Messages}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats.dateFormat
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object DateOfBirthSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(DateOfBirthPage).map { answer =>
      implicit val lang: Lang = messages.lang

      SummaryListRowViewModel(
        key = "dateOfBirth.checkYourAnswersLabel",
        value = ValueViewModel(answer.format(dateFormat())),
        actions = Seq(
          ActionItemViewModel("site.change", routes.DateOfBirthController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("dateOfBirth.change.hidden"))
        )
      )
    }
}
