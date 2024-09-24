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

import base.SpecBase
import models.BusinessType.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}
import play.api.test.Helpers.stubMessages

class BusinessTypeSpec extends SpecBase with ScalaCheckPropertyChecks with OptionValues {

  "BusinessType" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(BusinessType.values)

      forAll(gen) {
        businessType =>

          JsString(businessType.toString).validate[BusinessType].asOpt.value mustEqual businessType
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!BusinessType.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[BusinessType] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(BusinessType.values.toSeq)

      forAll(gen) {
        businessType =>

          Json.toJson(businessType) mustEqual JsString(businessType.toString)
      }
    }

    "must give the correct options for Platform Operators" in {

      val result = BusinessType.options(RegistrationType.PlatformOperator)(stubMessages())

      result.flatMap(_.value) mustEqual Seq(LimitedCompany, Partnership, Llp, AssociationOrTrust).map(_.toString)
    }

    "must give the correct options for Third Parties" in {

      val result = BusinessType.options(RegistrationType.ThirdParty)(stubMessages())

      result.flatMap(_.value) mustEqual Seq(LimitedCompany, Partnership, Llp, AssociationOrTrust, SoleTrader, Individual).map(_.toString)
    }
  }
}
