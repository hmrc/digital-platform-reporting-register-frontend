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
import models.subscription.responses.{AlreadySubscribedResponse, SubscribedResponse, SubscriptionResponse}
import models.subscription.{Contact, IndividualContact, OrganisationContact}
import models.{Mode, UserAnswers}
import pages.*
import play.api.data.Form

case class RegistrationConfirmationViewModel(
                                              mode: Mode,
                                              form: Form[Boolean],
                                              dprsUserId: String,
                                              primaryEmail: String,
                                              secondaryEmail: Option[String],
                                              isThirdParty: Boolean,
                                              isPrivateBeta: Boolean
                                            )

object RegistrationConfirmationViewModel {

  def apply(mode: Mode, userAnswers: UserAnswers, form: Form[Boolean], isPrivateBeta: Boolean): Option[RegistrationConfirmationViewModel] = {
    val details = userAnswers.subscriptionDetails

    val subscriptionRequest = details.map(_.subscriptionRequest)
    val subscriptionResponse = details.map(_.subscriptionResponse)

    val dprsUserId = subscriptionResponse match {
      case Some(response) => response match {
        case SubscribedResponse(dprsUserId) => Some(dprsUserId)
        case AlreadySubscribedResponse() => None
      }
      case None => None
    }

    dprsUserId.map { dprsUserId =>
      val optAnswerValue = userAnswers.get(RegistrationConfirmationPage)
      val primaryEmail = subscriptionRequest.flatMap(r => getEmail(Some(r.primaryContact))).getOrElse("")
      val secondaryEmail = subscriptionRequest.flatMap(r => getEmail(r.secondaryContact))
      val isThirdParty = details.map(_.registrationType).contains(ThirdParty)

      RegistrationConfirmationViewModel(
        mode = mode,
        form = optAnswerValue.fold(form)(answerValue => if (form.hasErrors) form else form.fill(answerValue)),
        dprsUserId = dprsUserId,
        primaryEmail = primaryEmail,
        secondaryEmail = secondaryEmail,
        isThirdParty = isThirdParty,
        isPrivateBeta = isPrivateBeta
      )
    }
  }
  
  private def getEmail(contact: Option[Contact]): Option[String] =
    contact.map {
      case IndividualContact(_, email, _) => email
      case OrganisationContact(_, email, _) => email
    }
}
