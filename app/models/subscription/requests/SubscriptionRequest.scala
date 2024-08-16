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

import cats.data.EitherNec
import cats.implicits.*
import models.pageviews.CheckYourAnswersOrganisationViewModel
import models.registration.Address
import models.subscription.{Contact, Organisation, OrganisationContact}
import models.{BusinessAddress, Country, InternationalAddress, UserAnswers}
import pages.{BusinessAddressPage, *}
import play.api.i18n.Messages
import play.api.libs.json.*
import queries.Query

final case class SubscriptionRequest(safeId: String,
                                     gbUser: Boolean,
                                     tradingName: Option[String],
                                     primaryContact: Contact,
                                     secondaryContact: Option[Contact])

object SubscriptionRequest {
  implicit lazy val writes: OWrites[SubscriptionRequest] = Json.format

  def build(safeId: String,
            userAnswers: UserAnswers,
            orgWithIdAddress: Option[Address])
           (implicit messages: Messages): EitherNec[Query, SubscriptionRequest] = {

    lazy val getOrgWithoutIdIsGb = (businessAddress: BusinessAddress) =>
      Some(Country.ukCountries.contains(businessAddress.country))

    lazy val getOrgWithIdIsGb = (address: Address) => Country.ukCountries.exists(_.code == address.countryCode)

    val maybeIsGb: Option[Boolean] = userAnswers.get(BusinessAddressPage)
      .fold(orgWithIdAddress.map(getOrgWithIdIsGb))(getOrgWithoutIdIsGb)

    val isGb = maybeIsGb.getOrElse(false)

    val secondaryContactInfo = for {
      secondaryContactName <- userAnswers.get(SecondaryContactNamePage)
      secondaryEmailAddress <- userAnswers.get(SecondaryContactEmailAddressPage)
    } yield {
      OrganisationContact(
        Organisation(name = secondaryContactName),
        email = secondaryEmailAddress,
        phone = userAnswers.get(SecondaryContactPhoneNumberPage)
      )
    }

    (
      userAnswers.getEither(PrimaryContactNamePage),
      userAnswers.getEither(PrimaryContactEmailAddressPage),
    ).parMapN{ (primaryContactName, primaryEmailAddress) =>
      SubscriptionRequest(
        safeId,
        gbUser = isGb,
        tradingName = userAnswers.get(BusinessEnterTradingNamePage),
        primaryContact = OrganisationContact(
          Organisation(name = primaryContactName),
          email = primaryEmailAddress,
          phone = userAnswers.get(PrimaryContactPhoneNumberPage)
        ),
        secondaryContact = secondaryContactInfo
      )
    }
  }

  private def getModel(userAnswers: UserAnswers)
                      (implicit messages: Messages): Option[CheckYourAnswersOrganisationViewModel] = {
    CheckYourAnswersOrganisationViewModel
      .apply(userAnswers)
  }
}
