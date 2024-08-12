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
import models.{Mode, UserAnswers}
import pages.{IndividualEmailAddressPage, PrimaryContactEmailAddressPage, RegistrationConfirmationPage, RegistrationTypePage, SecondaryContactEmailAddressPage}
import play.api.data.Form
import models.subscription.responses.{AlreadySubscribedResponse, SubscribedResponse, SubscriptionResponse}

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
    val dprsUserId = userAnswers.subscriptionResponse match {
      case Some(value) => value match {
        case SubscribedResponse(dprsId) => Some(dprsId)
        case AlreadySubscribedResponse() => None
      }
      case None => None
    }

    dprsUserId.map { dprsUserId =>
      val optAnswerValue = userAnswers.get(RegistrationConfirmationPage)
      val primaryEmail = userAnswers.get(PrimaryContactEmailAddressPage)
        .fold(userAnswers.get(IndividualEmailAddressPage).getOrElse(""))(identity)
      val secondaryEmail = userAnswers.get(SecondaryContactEmailAddressPage)
      val isThirdParty = userAnswers.get(RegistrationTypePage).contains(ThirdParty)

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
}
