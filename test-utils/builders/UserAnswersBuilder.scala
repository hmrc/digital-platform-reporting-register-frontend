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

package builders

import builders.MatchResponseWithIdBuilder.aMatchResponseWithId
import builders.SubscriptionDetailsBuilder.aSubscriptionDetails
import builders.UserBuilder.aUser
import models.UserAnswers
import play.api.libs.json.Json

object UserAnswersBuilder {

  val aUserAnswers: UserAnswers = UserAnswers(
    user = aUser,
    registrationResponse = Some(aMatchResponseWithId),
    subscriptionDetails = Some(aSubscriptionDetails),
    data = Json.obj()
  )

  val anEmptyAnswer: UserAnswers = aUserAnswers.copy(
    registrationResponse = None,
    subscriptionDetails = None,
    data = Json.obj())
}