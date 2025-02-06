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

package models.eacd

import models.{Nino, UserAnswers, Utr}
import pages.*

case class EnrolmentKnownFacts(providerId: String,
                               verifierKey: String,
                               verifierValue: String,
                               groupId: String)

object EnrolmentKnownFacts {

  def apply(userAnswers: UserAnswers): Option[EnrolmentKnownFacts] = {
    lazy val userAnswersNino = userAnswers.get(NinoPage)
    lazy val userAnswersUtr = userAnswers.get(UtrPage)
    lazy val userAnswersPostcode: Option[String] = userAnswers.get(BusinessAddressPage).map(_.postalCode)
      .orElse(userAnswers.get(UkAddressPage).map(_.postCode))
      .orElse(userAnswers.get(JerseyGuernseyIoMAddressPage).map(_.postCode))
      .orElse(userAnswers.get(InternationalAddressPage).map(_.postal))

    val verifierPair = userAnswers.user.taxIdentifier match {
      case Some(Nino(nino)) => Some("NINO" -> nino)
      case Some(Utr(utr)) => Some("UTR" -> utr)
      case None => userAnswersUtr.map(utr => "UTR" -> utr)
        .orElse(userAnswersNino.map(nino => "NINO" -> nino))
        .orElse(userAnswersPostcode.map(postcode => "Postcode" -> postcode))
    }

    verifierPair.map { verifierKeyValue =>
      EnrolmentKnownFacts(
        providerId = userAnswers.user.providerId.get,
        verifierKey = verifierKeyValue._1,
        verifierValue = verifierKeyValue._2,
        groupId = userAnswers.user.groupId.get
      )
    }
  }
}