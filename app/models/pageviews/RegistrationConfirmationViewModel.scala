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

package models.pageviews

import models.RegistrationType.ThirdParty
import models.subscription.responses.{SubscribedResponse, SubscriptionResponse}
import models.subscription.{Contact, IndividualContact, OrganisationContact}
import models.{Mode, UserAnswers}
import pages.*
import play.api.data.Form

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

case class RegistrationConfirmationViewModel(mode: Mode,
                                             form: Form[Boolean],
                                             dprsUserId: String,
                                             subscribedDateTime: String,
                                             primaryEmail: String,
                                             secondaryEmail: Option[String],
                                             isThirdParty: Boolean,
                                             isPrivateBeta: Boolean,
                                             businessName: Option[String])

object RegistrationConfirmationViewModel {

  private val Formatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma (z)").withZone(ZoneId.of("GMT"))

  def apply(mode: Mode, userAnswers: UserAnswers, form: Form[Boolean], isPrivateBeta: Boolean): Option[RegistrationConfirmationViewModel] = {
    val details = userAnswers.subscriptionDetails
    val subscriptionRequest = details.map(_.subscriptionRequest)
    val subscriptionResponse = details.map(_.subscriptionResponse)

    val optionalDprsUserId: Option[(String, Instant)] = subscriptionResponse match {
      case Some(SubscribedResponse(dprsUserId, subscribedDateTime)) => Some(dprsUserId, subscribedDateTime)
      case _ => None
    }

    optionalDprsUserId.map { dprsUserId =>
      val optAnswerValue = userAnswers.get(RegistrationConfirmationPage)
      val primaryEmail = subscriptionRequest.flatMap(r => getEmail(Some(r.primaryContact))).getOrElse("")
      val secondaryEmail = subscriptionRequest.flatMap(r => getEmail(r.secondaryContact))
      val isThirdParty = details.map(_.registrationType).contains(ThirdParty)
      val businessName = details.flatMap(_.businessName)
      val subscribedDateTime = Formatter.format(dprsUserId._2).replace("AM", "am").replace("PM", "pm")

      RegistrationConfirmationViewModel(
        mode = mode,
        form = optAnswerValue.fold(form)(answerValue => if (form.hasErrors) form else form.fill(answerValue)),
        dprsUserId = dprsUserId._1,
        subscribedDateTime = subscribedDateTime,
        primaryEmail = primaryEmail,
        secondaryEmail = secondaryEmail,
        isThirdParty = isThirdParty,
        isPrivateBeta = isPrivateBeta,
        businessName = businessName
      )
    }
  }

  private def getEmail(contact: Option[Contact]): Option[String] = contact.map {
    case IndividualContact(_, email, _) => email
    case OrganisationContact(_, email, _) => email
  }
}
