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

package models.audit

import base.SpecBase
import builders.MatchResponseWithIdBuilder.aMatchResponseWithId
import builders.UserAnswersBuilder.aUserAnswers

class AuditEventModelSpec extends SpecBase {

  private val underTest = AuditEventModel

  ".apply" - {
    "must return Subscription audit event when registration response exists in answers" in {
      val anyRegistrationResponse = aMatchResponseWithId
      val answers = aUserAnswers.copy(registrationResponse = Some(anyRegistrationResponse))

      underTest.apply(answers) mustBe AuditEventModel("Subscription", answers.data)
    }

    "must return AutoSubscription audit event when registration response does not exist in answers" in {
      val answers = aUserAnswers.copy(registrationResponse = None)

      underTest.apply(answers) mustBe AuditEventModel("AutoSubscription", answers.data)
    }
  }
}
