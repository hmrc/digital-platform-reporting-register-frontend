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
import pages.{CanPhoneSecondaryContactPage, SecondaryContactNamePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object CanPhoneSecondaryContactSummary  {

  def row(answers: UserAnswers)
         (implicit messages: Messages): Option[SummaryListRow] =
    for {
      answer <- answers.get(CanPhoneSecondaryContactPage)
      contactName <- answers.get(SecondaryContactNamePage)
    } yield {
      val value = if (answer) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key = messages("canPhoneSecondaryContact.checkYourAnswersLabel", contactName),
        value = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel("site.change", routes.CanPhoneSecondaryContactController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("canPhoneSecondaryContact.change.hidden", contactName))
        )
      )
    }
}
