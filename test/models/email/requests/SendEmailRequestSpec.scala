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

package models.email.requests

import base.SpecBase
import builders.UserAnswersBuilder.aUserAnswers
import builders.UserBuilder.aUser
import models.BusinessType.{LimitedCompany, SoleTrader}
import models.{BusinessType, IndividualName, SoleTraderName, Utr}
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages.*

class SendEmailRequestSpec extends SpecBase
  with TryValues
  with OptionValues
  with EitherValues {

  private val underTest = SendEmailRequest

  ".apply(...)" - {
    "must create SendEmailRequest object" in {
      SendEmailRequest.apply("some.email@example.com", "some-dprs-id", "some-first-name some-last-name") mustBe SendEmailRequest(
        to = List("some.email@example.com"),
        templateId = "dprs_registration_submitted",
        parameters = Map("dprsId" -> "some-dprs-id", "name" -> "some-first-name some-last-name")
      )
    }
  }

  ".getPrimaryContactEmail(...)" - {
    "must return organisation primary email when tax identifier is utr and email populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = Some(Utr("1234567890"))))
        .set(PrimaryContactEmailAddressPage, "any.email@example.com").success.value

      underTest.getPrimaryContactEmail(answers) mustBe Right("any.email@example.com")
    }

    "must return organisation primary email when business type not Individual or SoleTrader and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, LimitedCompany).success.value
        .set(PrimaryContactEmailAddressPage, "any.email@example.com").success.value

      underTest.getPrimaryContactEmail(answers) mustBe Right("any.email@example.com")

    }

    "must return individual email when business type is Individual and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, BusinessType.Individual).success.value
        .set(IndividualEmailAddressPage, "any.email@example.com").success.value

      underTest.getPrimaryContactEmail(answers) mustBe Right("any.email@example.com")
    }

    "must return individual email when business type is Sole trader and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, SoleTrader).success.value
        .set(IndividualEmailAddressPage, "any.email@example.com").success.value

      underTest.getPrimaryContactEmail(answers) mustBe Right("any.email@example.com")
    }

    "must return errors when business type not provided" in {
      val answers = aUserAnswers
        .remove(BusinessTypePage).success.value
      val result = underTest.getPrimaryContactEmail(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(BusinessTypePage)
    }

    "must return errors when business type is Individual and email not provided" in {
      val answers = aUserAnswers
        .set(BusinessTypePage, BusinessType.Individual).success.value
        .remove(IndividualEmailAddressPage).success.value

      val result = underTest.getPrimaryContactEmail(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(IndividualEmailAddressPage)
    }

    "must return errors when business type is Sole trader and email not provided" in {
      val answers = aUserAnswers
        .set(BusinessTypePage, BusinessType.SoleTrader).success.value
        .remove(IndividualEmailAddressPage).success.value

      val result = underTest.getPrimaryContactEmail(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(IndividualEmailAddressPage)
    }

    "must return errors when business type is not Individual or Sole trader and email not provided" in {
      val answers = aUserAnswers
        .set(BusinessTypePage, LimitedCompany).success.value
        .remove(PrimaryContactEmailAddressPage).success.value

      val result = underTest.getPrimaryContactEmail(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(PrimaryContactEmailAddressPage)
    }
  }

  ".getPrimaryContactName(...)" - {
    "must return organisation primary name when tax identifier is utr and name populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = Some(Utr("1234567890"))))
        .set(PrimaryContactNamePage, "any-contact-name").success.value

      underTest.getPrimaryContactName(answers) mustBe Right("any-contact-name")
    }

    "must return organisation primary name when business type not Individual or SoleTrader and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, LimitedCompany).success.value
        .set(PrimaryContactNamePage, "any-contact-name").success.value

      underTest.getPrimaryContactName(answers) mustBe Right("any-contact-name")

    }

    "must return individual name when business type is Individual and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, BusinessType.Individual).success.value
        .set(IndividualNamePage, IndividualName("any-first-name", "any-last-name")).success.value

      underTest.getPrimaryContactName(answers) mustBe Right("any-first-name any-last-name")
    }

    "must return individual name when business type is Sole trader and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, SoleTrader).success.value
        .set(SoleTraderNamePage, SoleTraderName("any-first-name", "any-last-name")).success.value

      underTest.getPrimaryContactName(answers) mustBe Right("any-first-name any-last-name")
    }

    "must return errors when business type not provided" in {
      val answers = aUserAnswers
        .remove(BusinessTypePage).success.value
      val result = underTest.getPrimaryContactName(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(BusinessTypePage)
    }

    "must return errors when business type is Individual and getIndividualContactName fails" in {
      val answers = aUserAnswers
        .set(BusinessTypePage, BusinessType.Individual).success.value
        .remove(IndividualNamePage).success.value
        .remove(SoleTraderNamePage).success.value

      val result = underTest.getPrimaryContactName(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(SoleTraderNamePage)
    }

    "must return errors when business type is Sole trader and getIndividualContactName fails" in {
      val answers = aUserAnswers
        .set(BusinessTypePage, BusinessType.SoleTrader).success.value
        .remove(IndividualNamePage).success.value
        .remove(SoleTraderNamePage).success.value

      val result = underTest.getPrimaryContactName(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(SoleTraderNamePage)
    }

    "must return errors when business type is not Individual or Sole trader and name not provided" in {
      val answers = aUserAnswers
        .set(BusinessTypePage, LimitedCompany).success.value
        .remove(PrimaryContactNamePage).success.value

      val result = underTest.getPrimaryContactName(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(PrimaryContactNamePage)
    }
  }

  ".getIndividualContactName(...)" - {
    "must return individual name when exists in answers" in {
      val answers = aUserAnswers
        .set(IndividualNamePage, IndividualName("any-firs-name", "any-last-name")).success.value
      underTest.getIndividualContactName(answers) mustBe Right("any-firs-name any-last-name")
    }

    "must return Sole trader name when exists in answers and individual name does not exist" in {
      val answers = aUserAnswers
        .set(SoleTraderNamePage, SoleTraderName("any-firs-name", "any-last-name")).success.value
      underTest.getIndividualContactName(answers) mustBe Right("any-firs-name any-last-name")
    }

    "must return error when individual name and sole trader name not in answers" in {
      val answers = aUserAnswers
        .remove(IndividualNamePage).success.value
        .remove(SoleTraderNamePage).success.value
      val result = underTest.getIndividualContactName(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(SoleTraderNamePage)
    }
  }

  ".build(...)" - {
    "must return correct SendEmailRequest" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = Some(Utr("1234567890"))))
        .set(PrimaryContactEmailAddressPage, "any.email@example.com").success.value
        .set(PrimaryContactNamePage, "any-contact-name").success.value

      SendEmailRequest.build(answers, "some-dprs-id") mustBe Right(SendEmailRequest(
        to = List("any.email@example.com"),
        templateId = "dprs_registration_submitted",
        parameters = Map("dprsId" -> "some-dprs-id", "name" -> "any-contact-name")
      ))
    }
  }
}
