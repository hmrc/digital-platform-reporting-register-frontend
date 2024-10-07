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

import models.BusinessType.*
import models.{BusinessType, Mode, UserAnswers}
import pages.{BusinessNamePage, BusinessTypePage}
import play.api.data.Form

case class BusinessNameViewModel(mode: Mode,
                                 form: Form[String],
                                 businessType: BusinessType,
                                 showLimitedCompanyHint: Boolean)

object BusinessNameViewModel {

  def apply(mode: Mode, userAnswers: UserAnswers, form: Form[String]): BusinessNameViewModel = {
    val optAnswerValue = userAnswers.get(BusinessNamePage)

    BusinessNameViewModel(
      mode = mode,
      form = optAnswerValue.fold(form)(answerValue => if (form.hasErrors) form else form.fill(answerValue)),
      businessType = userAnswers.get(BusinessTypePage) match {
        case Some(Partnership) => Partnership
        case Some(AssociationOrTrust) => AssociationOrTrust
        case _ => LimitedCompany
      },
      showLimitedCompanyHint = userAnswers.get(BusinessTypePage) match {
        case Some(LimitedCompany) | Some(Llp) => true
        case _ => false
      }
    )
  }
}