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
import models.subscription.responses.SubscribedResponse
import models.{Mode, UserAnswers}
import pages.*
import play.api.data.Form

import java.time.ZoneId
import java.time.format.DateTimeFormatter

case class RegistrationConfirmationViewModel(mode: Mode,
                                             form: Form[Boolean],
                                             dprsId: String,
                                             subscribedDateTime: String,
                                             primaryEmail: String,
                                             secondaryEmail: Option[String],
                                             isThirdParty: Boolean,
                                             businessName: Option[String])

object RegistrationConfirmationViewModel {

  private val Formatter = DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma (z)").withZone(ZoneId.of("GMT"))

  def apply(mode: Mode, userAnswers: UserAnswers, form: Form[Boolean]): Option[RegistrationConfirmationViewModel] = {
    val optAnswerValue = userAnswers.get(RegistrationConfirmationPage)
    val subscriptionResponse = userAnswers.subscriptionDetails.map(_.subscriptionResponse).get.asInstanceOf[SubscribedResponse]
    val subscribedDateTime = Formatter.format(subscriptionResponse.subscribedDateTime).replace("AM", "am").replace("PM", "pm")

    userAnswers.subscriptionDetails
      .map(_.subscriptionRequest)
      .map { subscriptionRequest =>
        RegistrationConfirmationViewModel(
          mode = mode,
          form = optAnswerValue.fold(form)(answerValue => if (form.hasErrors) form else form.fill(answerValue)),
          dprsId = subscriptionResponse.dprsId,
          subscribedDateTime = subscribedDateTime,
          primaryEmail = subscriptionRequest.primaryContact.email,
          secondaryEmail = subscriptionRequest.secondaryContact.map(_.email),
          isThirdParty = userAnswers.subscriptionDetails.map(_.registrationType).contains(ThirdParty),
          businessName = userAnswers.subscriptionDetails.flatMap(_.businessName)
        )
      }
  }
}
