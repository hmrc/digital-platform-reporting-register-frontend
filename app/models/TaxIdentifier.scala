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

import play.api.libs.functional.syntax.*
import play.api.libs.json.{Json, OFormat, OWrites, Reads}

sealed trait TaxIdentifier {
  val value: String
}

object TaxIdentifier {

  implicit lazy val reads: Reads[TaxIdentifier] =
    Nino.format.widen or Utr.format.widen

  implicit lazy val writes: OWrites[TaxIdentifier] = OWrites[TaxIdentifier] {
    case n: Nino => Json.toJsObject(n)(Nino.format)
    case u: Utr  => Json.toJsObject(u)(Utr.format)
  }
}

final case class Nino(nino: String) extends TaxIdentifier { override val value: String = nino }

object Nino {

  implicit lazy val format: OFormat[Nino] = Json.format
}

final case class Utr(utr: String) extends TaxIdentifier { override val value: String = utr }

object Utr {
  
  implicit lazy val format: OFormat[Utr] = Json.format
}
