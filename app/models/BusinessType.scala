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

sealed trait BusinessType

object BusinessType extends Enumerable.Implicits {

  case object LimitedCompany extends WithName("limitedCompany") with BusinessType
  case object Partnership extends WithName("partnership") with BusinessType
  case object Llp extends WithName("llp") with BusinessType
  case object AssociationOrTrust extends WithName("associationOrTrust") with BusinessType
  case object SoleTrader extends WithName("soleTrader") with BusinessType
  case object Individual extends WithName("individual") with BusinessType

  val values: Seq[BusinessType] = Seq(
    LimitedCompany, Partnership, Llp, AssociationOrTrust, SoleTrader, Individual
  )
  
  def valuesForRegistrationType(registrationType: RegistrationType): Seq[BusinessType] =
    registrationType match {
      case RegistrationType.PlatformOperator =>
        Seq(LimitedCompany, Partnership, Llp, AssociationOrTrust)
      case RegistrationType.ThirdParty =>
        Seq(LimitedCompany, Partnership, Llp, AssociationOrTrust, SoleTrader, Individual)
    }

  def options(registrationType: RegistrationType)(implicit messages: Messages): Seq[RadioItem] =
    valuesForRegistrationType(registrationType).zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"businessType.${value.toString}")),
          value   = Some(value.toString),
          id      = Some(s"value_$index")
        )
    }

  implicit val enumerable: Enumerable[BusinessType] =
    Enumerable(values.map(v => v.toString -> v) *)
}
