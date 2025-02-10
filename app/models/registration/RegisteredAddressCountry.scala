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

package models.registration

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.govuk.all.HintViewModel

sealed trait RegisteredAddressCountry

object RegisteredAddressCountry extends Enumerable.Implicits {

  case object Uk extends WithName("uk") with RegisteredAddressCountry
  case object JerseyGuernseyIsleOfMan extends WithName("jerseyGuernseyIsleOfMan") with RegisteredAddressCountry
  case object International extends WithName("international") with RegisteredAddressCountry

  val values: Seq[RegisteredAddressCountry] = Seq(
    Uk, JerseyGuernseyIsleOfMan, International
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"addressInUk.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index"),
        hint    = value.toString match {
          case RegisteredAddressCountry.Uk.toString => Some(HintViewModel(Text(messages(s"addressInUk.${value.toString}.hint"))))
          case _ => None
        }
      )
  }

  implicit val enumerable: Enumerable[RegisteredAddressCountry] =
    Enumerable(values.map(v => v.toString -> v) *)
}