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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.govuk.all.HintViewModel

sealed trait RegistrationType

object RegistrationType extends Enumerable.Implicits {

  case object PlatformOperator extends WithName("platformOperator") with RegistrationType
  case object ThirdParty extends WithName("thirdParty") with RegistrationType

  val values: Seq[RegistrationType] = Seq(
    PlatformOperator, ThirdParty
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"registrationType.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index"),
        hint    = Some(HintViewModel(Text(messages(s"registrationType.${value.toString}.hint"))))
      )
  }

  implicit val enumerable: Enumerable[RegistrationType] =
    Enumerable(values.map(v => v.toString -> v) *)
}
