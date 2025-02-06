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

package models.pageviews

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.checkAnswers.*

final case class CheckYourAnswersIndividualViewModel(contactDetails: SummaryList,
                                                     yourDetails: Option[SummaryList])

object CheckYourAnswersIndividualViewModel {

  def apply(answers: UserAnswers)(implicit messages: Messages): CheckYourAnswersIndividualViewModel = {
    val contactDetails = SummaryList(rows = Seq(
      IndividualEmailAddressSummary.row(answers),
      CanPhoneIndividualSummary.row(answers),
      IndividualPhoneNumberSummary.row(answers)
    ).flatten)
    val yourDetails = answers.registrationResponse match
      case Some(value) => None
      case None => Some(SummaryList(rows = Seq(
        IndividualNameSummary.row(answers),
        DateOfBirthSummary.row(answers),
        AddressInUkSummary.row(answers),
        InternationalAddressSummary.row(answers),
        UkAddressSummary.row(answers),
        JerseyGuernseyIoMAddressSummary.row(answers)
      ).flatten))

    CheckYourAnswersIndividualViewModel(contactDetails, yourDetails)
  }
}
