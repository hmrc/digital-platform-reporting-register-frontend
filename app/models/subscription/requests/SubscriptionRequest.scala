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

import cats.data.*
import cats.implicits.*
import models.BusinessType.SoleTrader
import models.registration.RegisteredAddressCountry.International
import models.subscription.*
import models.{BusinessType, SoleTraderName, UserAnswers, Utr}
import pages.*
import play.api.libs.json.*
import queries.Query

final case class SubscriptionRequest(id: String,
                                     gbUser: Boolean,
                                     tradingName: Option[String],
                                     primaryContact: Contact,
                                     secondaryContact: Option[Contact])

object SubscriptionRequest {
  implicit lazy val format: OFormat[SubscriptionRequest] = Json.format

  def build(safeId: String, answers: UserAnswers): EitherNec[Query, SubscriptionRequest] =
    (
      getGbUser(answers),
      getTradingName(answers),
      getPrimaryContactDetails(answers),
      getSecondaryContactDetails(answers)
    ).parMapN(SubscriptionRequest.apply(safeId, _, _, _, _))

  private[requests] def getGbUser(answers: UserAnswers): EitherNec[Query, Boolean] = answers.user.taxIdentifier match {
    case Some(_) => Right(true)
    case _ => answers.getEither(BusinessTypePage).flatMap {
      case BusinessType.Individual => answers.getEither(HasNinoPage).flatMap {
        case true => Right(true)
        case false => answers.getEither(AddressInUkPage).flatMap {
          case International => Right(false)
          case _ => Right(true)
        }
      }
      case _ => answers.getEither(RegisteredInUkPage)
    }
  }

  private[requests] def getTradingName(answers: UserAnswers): EitherNec[Query, Option[String]] =
    Right(answers.get(BusinessEnterTradingNamePage))

  private[requests] def getPrimaryContactDetails(answers: UserAnswers): EitherNec[Query, Contact] = answers.user.taxIdentifier match {
    case Some(Utr(_)) => getOrganisationPrimaryContactDetails(answers)
    case _ => answers.getEither(BusinessTypePage).flatMap {
      case BusinessType.Individual | SoleTrader => getIndividualContactDetails(answers)
      case _ => getOrganisationPrimaryContactDetails(answers)
    }
  }

  private[requests] def getOrganisationPrimaryContactDetails(answers: UserAnswers): EitherNec[Query, OrganisationContact] =
    (
      answers.getEither(PrimaryContactNamePage).map(Organisation(_)),
      answers.getEither(PrimaryContactEmailAddressPage),
      getOrganisationPrimaryContactPhoneNumber(answers)
    ).parMapN(OrganisationContact.apply)

  private[requests] def getOrganisationPrimaryContactPhoneNumber(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(CanPhonePrimaryContactPage).flatMap {
      case true => answers.getEither(PrimaryContactPhoneNumberPage).map(Some(_))
      case false => Right(None)
    }

  private[requests] def getIndividualContactDetails(answers: UserAnswers): EitherNec[Query, IndividualContact] =
    (
      getIndividualContactName(answers),
      answers.getEither(IndividualEmailAddressPage),
      getIndividualPhoneNumber(answers)
    ).parMapN(IndividualContact.apply)

  private[requests] def getIndividualContactName(answers: UserAnswers): EitherNec[Query, Individual] =
    answers.get(IndividualNamePage)
      .map(individualName => Right(Individual(individualName)))
      .getOrElse(answers.getEither(SoleTraderNamePage).map(Individual(_)))

  private[requests] def getIndividualPhoneNumber(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(CanPhoneIndividualPage).flatMap {
      case true => answers.getEither(IndividualPhoneNumberPage).map(Some(_))
      case false => Right(None)
    }

  private[requests] def getSecondaryContactDetails(answers: UserAnswers): EitherNec[Query, Option[Contact]] =
    answers.user.taxIdentifier match {
      case Some(Utr(_)) => getOrganisationSecondaryContactDetails(answers)
      case _ => answers.getEither(BusinessTypePage).flatMap {
        case BusinessType.Individual | SoleTrader => Right(None)
        case _ => getOrganisationSecondaryContactDetails(answers)
      }
    }

  private[requests] def getOrganisationSecondaryContactDetails(answers: UserAnswers): EitherNec[Query, Option[OrganisationContact]] =
    answers.getEither(HasSecondaryContactPage).flatMap {
      case false => Right(None)
      case true => (
        answers.getEither(SecondaryContactNamePage).map(Organisation(_)),
        answers.getEither(SecondaryContactEmailAddressPage),
        getOrganisationSecondaryContactPhoneNumber(answers)
      ).parMapN(OrganisationContact.apply).map(Some(_))
    }

  private[requests] def getOrganisationSecondaryContactPhoneNumber(answers: UserAnswers): EitherNec[Query, Option[String]] =
    answers.getEither(CanPhoneSecondaryContactPage).flatMap {
      case true => answers.getEither(SecondaryContactPhoneNumberPage).map(Some(_))
      case false => Right(None)
    }

  final case class BuildSubscriptionRequestFailure(errors: NonEmptyChain[Query]) extends Throwable {
    override def getMessage: String = s"Unable to build a subscription request, path(s) missing: ${errors.toChain.toList.map(_.path).mkString(", ")}"
  }
}
