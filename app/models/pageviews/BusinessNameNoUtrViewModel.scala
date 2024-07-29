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
import pages.BusinessNameNoUtrPage
import play.api.data.Form

case class BusinessNameNoUtrViewModel(mode: Mode, form: Form[String])

object BusinessNameNoUtrViewModel {

  def apply(mode: Mode, userAnswers: UserAnswers, form: Form[String]): BusinessNameNoUtrViewModel = {
    val optAnswerValue = userAnswers.get(BusinessNameNoUtrPage)

    BusinessNameNoUtrViewModel(
      mode = mode,
      form = optAnswerValue.fold(form)(answerValue => if (form.hasErrors) form else form.fill(answerValue))
    )
  }
}