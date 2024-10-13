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

import models.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryNino: Gen[String] =
    for {
      firstChar <- Gen.oneOf('A', 'C', 'E', 'H', 'J', 'L', 'M', 'O', 'P', 'R', 'S', 'W', 'X', 'Y').map(_.toString)
      secondChar <- Gen.oneOf('A', 'B', 'C', 'E', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'W', 'X', 'Y', 'Z').map(_.toString)
      digits <- Gen.listOfN(6, Gen.numChar)
      lastChar <- Gen.oneOf('A', 'B', 'C', 'D')
    } yield firstChar ++ secondChar ++ (digits :+ lastChar).mkString

  def ukPostcodeGen: Gen[String] =
    for {
      firstChars <- Gen.choose(1, 2)
      first <- Gen.listOfN(firstChars, Gen.alphaUpperChar).map(_.mkString)
      second <- Gen.numChar.map(_.toString)
      third <- Gen.oneOf(Gen.alphaUpperChar, Gen.numChar).map(_.toString)
      fourth <- Gen.numChar.map(_.toString)
      fifth <- Gen.listOfN(2, Gen.alphaUpperChar).map(_.mkString)
    } yield s"$first$second$third$fourth$fifth"

  implicit lazy val arbitraryBusinessAddress: Arbitrary[BusinessAddress] =
    Arbitrary {
      for {
        addressLine1 <- arbitrary[String]
        addressLine2 <- arbitrary[Option[String]]
        city <- arbitrary[String]
        region <- arbitrary[Option[String]]
        postalCode <- arbitrary[String]
        country <- Gen.oneOf(Country.nonUkInternationalCountries)
      } yield BusinessAddress(addressLine1, addressLine2, city, region, postalCode, country)
    }

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] =
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- arbitrary[String]
        town <- arbitrary[String]
        county <- arbitrary[String]
        postCode <- arbitrary[String]
        country <- Gen.oneOf(Country.ukCountries)
      } yield UkAddress(line1, Some(line2), town, Some(county), postCode, country)
    }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- arbitrary[Option[String]]
        city <- arbitrary[String]
        region <- arbitrary[Option[String]]
        postal <- arbitrary[String]
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
