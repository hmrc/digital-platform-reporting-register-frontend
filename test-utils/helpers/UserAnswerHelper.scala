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

package helpers

import models.UserAnswers
import models.registration.Address
import models.registration.responses.MatchResponseWithId

import java.util.UUID

trait UserAnswerHelper {

  implicit class UserAnswerHelper(answers: UserAnswers) {
    def withBusiness(name: String, address: Address) = answers.copy(
      registrationResponse = Some(MatchResponseWithId(
        UUID.randomUUID().toString(),
        address,
        Some(name)
      ))
    )
  }
}
