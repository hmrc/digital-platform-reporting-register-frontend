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

import models.registration.responses.MatchResponseWithId
import models.UserAnswers
import pages.{BusinessNameNoUtrPage, BusinessNamePage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.checkAnswers.*

final case class CheckYourAnswersOrganisationViewModel(businessDetailList: SummaryList,
                                                       primaryContactList: SummaryList,
                                                       secondaryContactList: SummaryList,
                                                       businessName: String,
                                                       hasRegistered: Boolean
                                                      )

object CheckYourAnswersOrganisationViewModel {
  
  def apply(answers: UserAnswers)(implicit messages: Messages): Option[CheckYourAnswersOrganisationViewModel] = {
    val name: Option[String] = answers.registrationResponse.flatMap {
      case MatchResponseWithId(_, _, organisationName) => organisationName
      case _ => None
    } orElse answers.get(BusinessNamePage) match {
      case None => answers.get(BusinessNameNoUtrPage)
      case Some(value) => Some(value)
    }

    val businessDetailList = SummaryList(rows = Seq(
      BusinessNameNoUtrSummary.row(answers),
      HasBusinessTradingNameSummary.row(answers),
      BusinessEnterTradingNameSummary.row(answers),
      BusinessAddressSummary.row(answers)
    ).flatten)

    val primaryContactList = SummaryList(rows = Seq(
      PrimaryContactNameSummary.row(answers),
      PrimaryContactEmailAddressSummary.row(answers),
      CanPhonePrimaryContactSummary.row(answers),
      PrimaryContactPhoneNumberSummary.row(answers)
    ).flatten)
    
    val secondaryContactList = SummaryList(rows = Seq(
      HasSecondaryContactSummary.row(answers),
      SecondaryContactNameSummary.row(answers),
      SecondaryContactEmailAddressSummary.row(answers),
      CanPhoneSecondaryContactSummary.row(answers),
      SecondaryContactPhoneNumberSummary.row(answers)
    ).flatten)
    
    name.map {
      val hasRegistered = answers.registrationResponse.isDefined
      CheckYourAnswersOrganisationViewModel(businessDetailList, primaryContactList, secondaryContactList, _, hasRegistered)
    }
  }
}
