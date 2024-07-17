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

package views

import models.{Country, InternationalAddress, UkAddress}
import models.registration.Address
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.mustEqual

class ViewUtilsSpec extends AnyFreeSpec {

  "must format an address properly" - {

    val line1   = "   31 Grand Boulevard   "
    val line2   = ""
    val city    = "Alabama"
    val postal  = "AL1"
    val country = Country.unitedKingdom

    val expectedResult = Seq(
      line1,
      city,
      postal,
      country.name
    ).map(_.trim).mkString("<br/>")

    "for an address as returned by ETMP" in {
      val address = Address(
        line1,
        Some(line2),
        None,
        Some(city),
        Some(postal),
        country.code
      )

      ViewUtils.formatAddress(address) mustEqual expectedResult
    }

    "for a UK address" in {
      val address = UkAddress(
        line1,
        Some(line2),
        city,
        None,
        postal
      )

      ViewUtils.formatUkAddress(address) mustEqual expectedResult
    }

    "for an international address" in {
      val address = InternationalAddress(
        line1,
        Some(line2),
        city,
        None,
        Some(postal),
        country
      )

      ViewUtils.formatInternationalAddress(address) mustEqual expectedResult
    }
  }
}
