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

package generators

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] =
    Arbitrary {
      for {
        line1    <- arbitrary[String]
        line2    <- arbitrary[String]
        town     <- arbitrary[String]
        county   <- arbitrary[String]
        postCode <- arbitrary[String]
      } yield UkAddress(line1, Some(line2), town, Some(county), postCode)
    }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        line1   <- arbitrary[String]
        line2   <- arbitrary[Option[String]]
        city    <- arbitrary[String]
        region  <- arbitrary[Option[String]]
        postal  <- arbitrary[Option[String]]
        country <- Gen.oneOf(Country.internationalCountries)
      } yield InternationalAddress(line1, line2, city, region, postal, country)
    }

  implicit lazy val arbitraryIndividualName: Arbitrary[IndividualName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName <- arbitrary[String]
      } yield IndividualName(firstName, lastName)
    }

  implicit lazy val arbitrarySoleTraderName: Arbitrary[SoleTraderName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName <- arbitrary[String]
      } yield SoleTraderName(firstName, lastName)
    }

  implicit lazy val arbitraryBusinessType: Arbitrary[BusinessType] =
    Arbitrary {
      Gen.oneOf(BusinessType.values)
    }

  implicit lazy val arbitraryRegistrationType: Arbitrary[RegistrationType] =
    Arbitrary {
      Gen.oneOf(RegistrationType.values)
    }
}
