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

case class EnrolmentDetails(providerId: String,
                            verifierKey: String,
                            verifierValue: String,
                            groupId: String,
                            identifier: Identifier)

object EnrolmentDetails {

  def apply(enrolmentKnownFacts: EnrolmentKnownFacts, dprsId: String): EnrolmentDetails = EnrolmentDetails(
    providerId = enrolmentKnownFacts.providerId,
    verifierKey = enrolmentKnownFacts.verifierKey,
    verifierValue = enrolmentKnownFacts.verifierValue,
    groupId = enrolmentKnownFacts.groupId,
    identifier = Identifier(dprsId)
  )
}