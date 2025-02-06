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

package models.subscription.requests

import base.SpecBase
import builders.UserAnswersBuilder.{aUserAnswers, anEmptyAnswer}
import builders.UserBuilder.aUser
import models.BusinessType.{LimitedCompany, SoleTrader}
import models.registration.RegisteredAddressCountry
import models.subscription.*
import models.{BusinessType, IndividualName, SoleTraderName, Utr}
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages.*

import scala.util.Right

class SubscriptionRequestSpec extends SpecBase
  with TryValues
  with OptionValues
  with EitherValues {

  private val underTest = SubscriptionRequest

  ".getTradingName" - {
    "must return business trading name when exists in answers" in {
      val answers = aUserAnswers.set(BusinessEnterTradingNamePage, "some-business-trading-name").success.value
      underTest.getTradingName(answers) mustBe Right(Some("some-business-trading-name"))
    }

    "must return None when does not exist in answers" in {
      val answers = aUserAnswers.remove(BusinessEnterTradingNamePage).success.value
      underTest.getTradingName(answers) mustBe Right(None)
    }
  }

  ".getOrganisationPrimaryContactPhoneNumber" - {
    "must return organisation primary contact phone number when exists in answers" in {
      val answers = aUserAnswers
        .set(CanPhonePrimaryContactPage, true).success.value
        .set(PrimaryContactPhoneNumberPage, "08432799643").success.value
      underTest.getOrganisationPrimaryContactPhoneNumber(answers) mustBe Right(Some("08432799643"))
    }

    "must return none when can not phone contact" in {
      val answers = aUserAnswers
        .set(CanPhonePrimaryContactPage, false).success.value
      underTest.getOrganisationPrimaryContactPhoneNumber(answers) mustBe Right(None)
    }

    "must return error when can phone, but has no phone number" in {
      val answers = aUserAnswers
        .set(CanPhonePrimaryContactPage, true).success.value
        .remove(PrimaryContactPhoneNumberPage).success.value
      val result = underTest.getOrganisationPrimaryContactPhoneNumber(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(PrimaryContactPhoneNumberPage)
    }
  }

  ".getOrganisationPrimaryContactDetails" - {
    "must return organisation primary contact details when data exists in answers" in {
      val answers = aUserAnswers
        .set(PrimaryContactNamePage, "any-contact-name").success.value
        .set(PrimaryContactEmailAddressPage, "any_email@example.com").success.value
        .set(CanPhonePrimaryContactPage, true).success.value
        .set(PrimaryContactPhoneNumberPage, "08432799643").success.value

      underTest.getOrganisationPrimaryContactDetails(answers) mustBe
        Right(OrganisationContact(Organisation("any-contact-name"), "any_email@example.com", Some("08432799643")))
    }

    "must return all errors when relevant data not provided" in {
      val result = underTest.getOrganisationPrimaryContactDetails(anEmptyAnswer)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        PrimaryContactNamePage,
        PrimaryContactEmailAddressPage,
        CanPhonePrimaryContactPage
      )
    }
  }

  ".getGbUser" - {
    "must return true when tax identifier is present" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = Some(Utr("any-utr"))))
      underTest.getGbUser(answers) mustBe Right(true)
    }

    "Individual business type" - {
      "must return true when has nino" in {
        val answers = aUserAnswers
          .set(BusinessTypePage, BusinessType.Individual).success.value
          .set(HasNinoPage, true).success.value
        underTest.getGbUser(answers) mustBe Right(true)
      }

      "must return true when has no nino and address is in UK" in {
        val answers = aUserAnswers
          .set(BusinessTypePage, BusinessType.Individual).success.value
          .set(HasNinoPage, false).success.value
          .set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
        underTest.getGbUser(answers) mustBe Right(true)
      }

      "must return true when has no nino and address is in JerseyGuernseyIoM" in {
        val answers = aUserAnswers
          .set(BusinessTypePage, BusinessType.Individual).success.value
          .set(HasNinoPage, false).success.value
          .set(AddressInUkPage, RegisteredAddressCountry.JerseyGuernseyIsleOfMan).success.value
        underTest.getGbUser(answers) mustBe Right(true)
      }

      "must return false when no nino and address is International" in {
        val answers = aUserAnswers
          .set(BusinessTypePage, BusinessType.Individual).success.value
          .set(HasNinoPage, false).success.value
          .set(AddressInUkPage, RegisteredAddressCountry.International).success.value
        underTest.getGbUser(answers) mustBe Right(false)
      }

      "must return error" - {
        "when HasNinoPage not answered" in {
          val answers = aUserAnswers
            .set(BusinessTypePage, BusinessType.Individual).success.value
            .remove(HasNinoPage).success.value
          val result = underTest.getGbUser(answers)
          result.left.value.toChain.toList must contain theSameElementsAs Seq(HasNinoPage)
        }

        "when does not have nino and AddressInUkPage not answered" in {
          val answers = aUserAnswers
            .set(BusinessTypePage, BusinessType.Individual).success.value
            .set(HasNinoPage, false).success.value
            .remove(AddressInUkPage).success.value
          val result = underTest.getGbUser(answers)
          result.left.value.toChain.toList must contain theSameElementsAs Seq(AddressInUkPage)
        }
      }
    }

    "Non individual business type" - {
      "must return true when registered in the UK" in {
        val answers = aUserAnswers
          .set(BusinessTypePage, LimitedCompany).success.value
          .set(RegisteredInUkPage, true).success.value
        underTest.getGbUser(answers) mustBe Right(true)
      }

      "must return false when not registered in the UK" in {
        val answers = aUserAnswers
          .set(BusinessTypePage, LimitedCompany).success.value
          .set(RegisteredInUkPage, false).success.value
        underTest.getGbUser(answers) mustBe Right(false)
      }

      "must return error when not RegisteredInUkPage not in answers" in {
        val answers = aUserAnswers
          .set(BusinessTypePage, LimitedCompany).success.value
          .remove(RegisteredInUkPage).success.value
        val result = underTest.getGbUser(answers)
        result.left.value.toChain.toList must contain theSameElementsAs Seq(RegisteredInUkPage)
      }
    }

    "must return error when tax identifier not present and has no BusinessTypePage" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .remove(BusinessTypePage).success.value
      val result = underTest.getGbUser(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(BusinessTypePage)
    }
  }

  ".getIndividualPhoneNumber" - {
    "must return individual phone number when exists in answers" in {
      val answers = aUserAnswers
        .set(CanPhoneIndividualPage, true).success.value
        .set(IndividualPhoneNumberPage, "08432799643").success.value
      underTest.getIndividualPhoneNumber(answers) mustBe Right(Some("08432799643"))
    }

    "must return none when can not phone contact" in {
      val answers = aUserAnswers
        .set(CanPhoneIndividualPage, false).success.value
      underTest.getIndividualPhoneNumber(answers) mustBe Right(None)
    }

    "must return error when can phone, but has no phone number" in {
      val answers = aUserAnswers
        .set(CanPhoneIndividualPage, true).success.value
        .remove(IndividualPhoneNumberPage).success.value
      val result = underTest.getIndividualPhoneNumber(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(IndividualPhoneNumberPage)
    }

    "must return error when CanPhoneIndividualPage not in answers" in {
      val answers = aUserAnswers
        .remove(CanPhoneIndividualPage).success.value
      val result = underTest.getIndividualPhoneNumber(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(CanPhoneIndividualPage)
    }
  }

  ".getIndividualContactName" - {
    "must return individual name when exists in answers" in {
      val answers = aUserAnswers
        .set(IndividualNamePage, IndividualName("any-firs-name", "any-last-name")).success.value
      underTest.getIndividualContactName(answers) mustBe Right(Individual("any-firs-name", "any-last-name"))
    }

    "must return Sole trader name when exists in answers and individual name does not exist" in {
      val answers = aUserAnswers
        .set(SoleTraderNamePage, SoleTraderName("any-firs-name", "any-last-name")).success.value
      underTest.getIndividualContactName(answers) mustBe Right(Individual("any-firs-name", "any-last-name"))
    }

    "must return error when individual name and sole trader name not in answers" in {
      val answers = aUserAnswers
        .remove(IndividualNamePage).success.value
        .remove(SoleTraderNamePage).success.value
      val result = underTest.getIndividualContactName(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(SoleTraderNamePage)
    }
  }

  ".getIndividualContactDetails" - {
    "must return individual contact details when relevant data provided" in {
      val answers = aUserAnswers
        .set(IndividualNamePage, IndividualName("any-firs-name", "any-last-name")).success.value
        .set(IndividualEmailAddressPage, "any.email@example.com").success.value
        .set(CanPhoneIndividualPage, true).success.value
        .set(IndividualPhoneNumberPage, "0123456789").success.value
      val expected = IndividualContact(
        individual = Individual("any-firs-name", "any-last-name"),
        email = "any.email@example.com",
        phone = Some("0123456789")
      )
      underTest.getIndividualContactDetails(answers) mustBe Right(expected)
    }

    "must return errors when data not provided" in {
      val answers = aUserAnswers
        .remove(IndividualNamePage).success.value
        .remove(SoleTraderNamePage).success.value
        .remove(IndividualEmailAddressPage).success.value
        .set(CanPhoneIndividualPage, true).success.value
        .remove(IndividualPhoneNumberPage).success.value
      val result = underTest.getIndividualContactDetails(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        SoleTraderNamePage,
        IndividualEmailAddressPage,
        IndividualPhoneNumberPage
      )
    }
  }

  ".getPrimaryContactDetails" - {
    "must return organisation primary data when tax identifier is utr and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = Some(Utr("1234567890"))))
        .set(PrimaryContactNamePage, "any-name").success.value
        .set(PrimaryContactEmailAddressPage, "any.email@example.com").success.value
        .set(CanPhonePrimaryContactPage, true).success.value
        .set(PrimaryContactPhoneNumberPage, "123456789").success.value
      val expected = OrganisationContact(
        organisation = Organisation("any-name"),
        email = "any.email@example.com",
        phone = Some("123456789")
      )
      underTest.getPrimaryContactDetails(answers) mustBe Right(expected)
    }

    "must return organisation primary data when business type not Individual or SoleTrader and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, LimitedCompany).success.value
        .set(PrimaryContactNamePage, "any-name").success.value
        .set(PrimaryContactEmailAddressPage, "any.email@example.com").success.value
        .set(CanPhonePrimaryContactPage, true).success.value
        .set(PrimaryContactPhoneNumberPage, "123456789").success.value
      val expected = OrganisationContact(
        organisation = Organisation("any-name"),
        email = "any.email@example.com",
        phone = Some("123456789")
      )
      underTest.getPrimaryContactDetails(answers) mustBe Right(expected)
    }

    "must return individual contact data when business type is Individual and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, BusinessType.Individual).success.value
        .set(IndividualNamePage, IndividualName("any-first-name", "any-last-name")).success.value
        .set(IndividualEmailAddressPage, "any.email@example.com").success.value
        .set(CanPhoneIndividualPage, true).success.value
        .set(IndividualPhoneNumberPage, "123456789").success.value
      val expected = IndividualContact(
        individual = Individual("any-first-name", "any-last-name"),
        email = "any.email@example.com",
        phone = Some("123456789")
      )
      underTest.getPrimaryContactDetails(answers) mustBe Right(expected)
    }

    "must return individual contact data when business type is Sole trader and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, SoleTrader).success.value
        .set(IndividualNamePage, IndividualName("any-first-name", "any-last-name")).success.value
        .set(IndividualEmailAddressPage, "any.email@example.com").success.value
        .set(CanPhoneIndividualPage, true).success.value
        .set(IndividualPhoneNumberPage, "123456789").success.value
      val expected = IndividualContact(
        individual = Individual("any-first-name", "any-last-name"),
        email = "any.email@example.com",
        phone = Some("123456789")
      )
      underTest.getPrimaryContactDetails(answers) mustBe Right(expected)
    }

    "must return errors when business type not provided" in {
      val answers = aUserAnswers
        .remove(BusinessTypePage).success.value
      val result = underTest.getPrimaryContactDetails(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(BusinessTypePage)
    }

    "must return errors when business type is Individual and other relevant data not provided" in {
      val answers = aUserAnswers
        .set(BusinessTypePage, BusinessType.Individual).success.value
        .remove(IndividualNamePage).success.value
        .remove(SoleTraderNamePage).success.value
        .remove(IndividualEmailAddressPage).success.value
        .set(CanPhoneIndividualPage, true).success.value
        .remove(IndividualPhoneNumberPage).success.value
      val result = underTest.getPrimaryContactDetails(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        SoleTraderNamePage,
        IndividualEmailAddressPage,
        IndividualPhoneNumberPage
      )
    }

    "must return errors when business type is Sole trader and other relevant data not provided" in {
      val answers = aUserAnswers
        .set(BusinessTypePage, BusinessType.SoleTrader).success.value
        .remove(IndividualNamePage).success.value
        .remove(SoleTraderNamePage).success.value
        .remove(IndividualEmailAddressPage).success.value
        .set(CanPhoneIndividualPage, true).success.value
        .remove(IndividualPhoneNumberPage).success.value
      val result = underTest.getPrimaryContactDetails(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        SoleTraderNamePage,
        IndividualEmailAddressPage,
        IndividualPhoneNumberPage
      )
    }

    "must return errors when business type is not Individual or Sole trader and other relevant data not provided" in {
      val answers = aUserAnswers
        .set(BusinessTypePage, LimitedCompany).success.value
        .remove(PrimaryContactNamePage).success.value
        .remove(PrimaryContactEmailAddressPage).success.value
        .set(CanPhonePrimaryContactPage, true).success.value
        .remove(PrimaryContactPhoneNumberPage).success.value
      val result = underTest.getPrimaryContactDetails(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        PrimaryContactNamePage,
        PrimaryContactEmailAddressPage,
        PrimaryContactPhoneNumberPage
      )
    }
  }

  ".getOrganisationSecondaryContactPhoneNumber" - {
    "must return organisation secondary contact phone number when exists in answers" in {
      val answers = aUserAnswers
        .set(CanPhoneSecondaryContactPage, true).success.value
        .set(SecondaryContactPhoneNumberPage, "08432799643").success.value
      underTest.getOrganisationSecondaryContactPhoneNumber(answers) mustBe Right(Some("08432799643"))
    }

    "must return none when can not phone contact" in {
      val answers = aUserAnswers
        .set(CanPhoneSecondaryContactPage, false).success.value
      underTest.getOrganisationSecondaryContactPhoneNumber(answers) mustBe Right(None)
    }

    "must return error when can phone, but has no phone number" in {
      val answers = aUserAnswers
        .set(CanPhoneSecondaryContactPage, true).success.value
        .remove(SecondaryContactPhoneNumberPage).success.value
      val result = underTest.getOrganisationSecondaryContactPhoneNumber(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(SecondaryContactPhoneNumberPage)
    }
  }

  ".getOrganisationSecondaryContactDetails" - {
    "must return None when has no secondary contact" in {
      val answers = aUserAnswers.set(HasSecondaryContactPage, false).success.value

      underTest.getOrganisationSecondaryContactDetails(answers) mustBe Right(None)
    }

    "must return organisation secondary contact details when data exists in answers" in {
      val answers = aUserAnswers
        .set(HasSecondaryContactPage, true).success.value
        .set(SecondaryContactNamePage, "any-contact-name").success.value
        .set(SecondaryContactEmailAddressPage, "any_email@example.com").success.value
        .set(CanPhoneSecondaryContactPage, true).success.value
        .set(SecondaryContactPhoneNumberPage, "08432799643").success.value

      underTest.getOrganisationSecondaryContactDetails(answers) mustBe
        Right(Some(OrganisationContact(Organisation("any-contact-name"), "any_email@example.com", Some("08432799643"))))
    }

    "must return all errors when relevant data not provided" in {
      val answers = anEmptyAnswer
        .set(HasSecondaryContactPage, true).success.value

      val result = underTest.getOrganisationSecondaryContactDetails(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        SecondaryContactNamePage,
        SecondaryContactEmailAddressPage,
        CanPhoneSecondaryContactPage
      )
    }
  }

  ".getSecondaryContactDetails" - {
    "must return organisation secondary data when tax identifier is utr and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = Some(Utr("1234567890"))))
        .set(HasSecondaryContactPage, true).success.value
        .set(SecondaryContactNamePage, "any-name").success.value
        .set(SecondaryContactEmailAddressPage, "any.email@example.com").success.value
        .set(CanPhoneSecondaryContactPage, true).success.value
        .set(SecondaryContactPhoneNumberPage, "123456789").success.value
      val expected = OrganisationContact(
        organisation = Organisation("any-name"),
        email = "any.email@example.com",
        phone = Some("123456789")
      )
      underTest.getSecondaryContactDetails(answers) mustBe Right(Some(expected))
    }

    "must return organisation secondary data when business type not Individual or SoleTrader and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, LimitedCompany).success.value
        .set(HasSecondaryContactPage, true).success.value
        .set(SecondaryContactNamePage, "any-name").success.value
        .set(SecondaryContactEmailAddressPage, "any.email@example.com").success.value
        .set(CanPhoneSecondaryContactPage, true).success.value
        .set(SecondaryContactPhoneNumberPage, "123456789").success.value
      val expected = OrganisationContact(
        organisation = Organisation("any-name"),
        email = "any.email@example.com",
        phone = Some("123456789")
      )
      underTest.getSecondaryContactDetails(answers) mustBe Right(Some(expected))
    }

    "must return no contact data when business type is Individual and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, BusinessType.Individual).success.value
      underTest.getSecondaryContactDetails(answers) mustBe Right(None)
    }

    "must return no contact data when business type is Sole trader and relevant data populated in answers" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = None))
        .set(BusinessTypePage, SoleTrader).success.value
      underTest.getSecondaryContactDetails(answers) mustBe Right(None)
    }

    "must return errors when business type not provided" in {
      val answers = aUserAnswers
        .remove(BusinessTypePage).success.value
      val result = underTest.getSecondaryContactDetails(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(BusinessTypePage)
    }

    "must return errors when business type is not Individual or Sole trader and other relevant data not provided" in {
      val answers = aUserAnswers
        .set(BusinessTypePage, LimitedCompany).success.value
        .set(HasSecondaryContactPage, true).success.value
        .remove(SecondaryContactNamePage).success.value
        .remove(SecondaryContactEmailAddressPage).success.value
        .set(CanPhoneSecondaryContactPage, true).success.value
        .remove(SecondaryContactPhoneNumberPage).success.value
      val result = underTest.getSecondaryContactDetails(answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        SecondaryContactNamePage,
        SecondaryContactEmailAddressPage,
        SecondaryContactPhoneNumberPage
      )
    }
  }

  ".build" - {
    "must return subscription request when relevant data provided" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = Some(Utr("any-utr"))))
        .set(PrimaryContactNamePage, "some-name").success.value
        .set(PrimaryContactEmailAddressPage, "some.email@example.com").success.value
        .set(CanPhonePrimaryContactPage, false).success.value
        .set(HasSecondaryContactPage, false).success.value

      val organisationContact = OrganisationContact(
        organisation = Organisation("some-name"),
        email = "some.email@example.com",
        phone = None,
      )
      val expected = SubscriptionRequest(
        id = "any-safe-id",
        gbUser = true,
        tradingName = None,
        primaryContact = organisationContact,
        secondaryContact = None
      )
      underTest.build("any-safe-id", answers) mustBe Right(expected)
    }

    "must return errors when relevant data provided" in {
      val answers = aUserAnswers.copy(user = aUser.copy(taxIdentifier = Some(Utr("any-utr"))))
        .remove(PrimaryContactNamePage).success.value

      val result = underTest.build("any-safe-if", answers)
      result.left.value.toChain.toList must contain theSameElementsAs Seq(
        PrimaryContactNamePage,
        PrimaryContactEmailAddressPage,
        CanPhonePrimaryContactPage,
        HasSecondaryContactPage
      )
    }
  }
}
