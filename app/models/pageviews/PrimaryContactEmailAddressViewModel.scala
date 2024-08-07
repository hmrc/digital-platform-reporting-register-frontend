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

import models.{Mode, UserAnswers}
import pages.PrimaryContactEmailAddressPage
import play.api.data.Form

case class PrimaryContactEmailAddressViewModel(mode: Mode, form: Form[String], contactName: String)

object PrimaryContactEmailAddressViewModel {

  def apply(mode: Mode, userAnswers: UserAnswers, form: Form[String], contactName: String): PrimaryContactEmailAddressViewModel = {
    val optAnswerValue = userAnswers.get(PrimaryContactEmailAddressPage)

    PrimaryContactEmailAddressViewModel(
      mode = mode,
      form = optAnswerValue.fold(form)(answerValue => if (form.hasErrors) form else form.fill(answerValue)),
      contactName = contactName
    )
  }
}