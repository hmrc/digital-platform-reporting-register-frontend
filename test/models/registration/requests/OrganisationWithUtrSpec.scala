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

package models.registration.requests

import builders.UserBuilder.aUser
import cats.data.*
import models.{BusinessType, Nino, UserAnswers, Utr}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages.{BusinessNamePage, BusinessTypePage, UtrPage}
import play.api.libs.json.Json

class OrganisationWithUtrSpec
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with EitherValues {

  "organisation with UTR" - {

    "must serialise with no details" in {

      val organisation = OrganisationWithUtr("123", None)
      val json = Json.toJson(organisation)

      json mustEqual Json.obj(
        "type" -> "organisation",
        "utr" -> "123"
      )
    }

    "must serialise with details" in {

      val organisation = OrganisationWithUtr("123", Some(OrganisationDetails("name", BusinessType.LimitedCompany)))
      val json = Json.toJson(organisation)

      json mustEqual Json.obj(
        "type" -> "organisation",
        "utr" -> "123",
        "details" -> Json.obj(
          "name" -> "name",
          "organisationType" -> "limitedCompany"
        )
      )
    }

    "must build when the user has a UTR in their tax identifier" in {
      val answers = UserAnswers(aUser.copy(taxIdentifier = Some(Utr("123"))))

      OrganisationWithUtr.build(answers).value mustEqual OrganisationWithUtr("123", None)
    }

    "must build when the user has a NINO in their tax identifier but has entered all necessary details" in {
      val answers = UserAnswers(aUser.copy(taxIdentifier = Some(Nino("123"))))
        .set(UtrPage, "123").success.value
        .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
        .set(BusinessNamePage, "name").success.value

      OrganisationWithUtr.build(answers).value mustEqual OrganisationWithUtr("123", Some(OrganisationDetails("name", BusinessType.LimitedCompany)))
    }

    "must build when the user has no tax identifier but has entered all necessary details" in {
      val answers = UserAnswers(aUser)
        .set(UtrPage, "123").success.value
        .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
        .set(BusinessNamePage, "name").success.value

      val result = OrganisationWithUtr.build(answers)
      result.value mustEqual OrganisationWithUtr("123", Some(OrganisationDetails("name", BusinessType.LimitedCompany)))
    }

    "must fail to build when the user has a Nino in their tax identifier and has not entered all necessary details" in {
      val answers = UserAnswers(aUser.copy(taxIdentifier = Some(Nino("123"))))

      val result = OrganisationWithUtr.build(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(UtrPage, BusinessTypePage, BusinessNamePage)
    }

    "must fail to build when the user has no tax identifier and has not entered all necessary details" in {
      val answers = UserAnswers(aUser)

      val result = OrganisationWithUtr.build(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(UtrPage, BusinessTypePage, BusinessNamePage)
    }
  }
}
