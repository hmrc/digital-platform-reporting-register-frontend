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

package controllers.actions

import base.SpecBase
import builders.UserBuilder.aUser
import models.requests.{IdentifierRequest, OptionalUserSessionDataRequest}
import models.{Nino, UserAnswers}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  class Harness(sessionRepository: SessionRepository) extends DataRetrievalActionImpl(sessionRepository) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalUserSessionDataRequest[A]] = transform(request)
  }

  "Data Retrieval Action" - {
    "when there is no data in the cache" - {
      "must set userAnswers to 'None' in the request" in {
        val sessionRepository = mock[SessionRepository]
        val action = new Harness(sessionRepository)

        when(sessionRepository.get(aUser)) thenReturn Future(None)

        action.callTransform(IdentifierRequest(aUser, FakeRequest())).futureValue.userAnswers must not be defined
      }
    }

    "when there is data in the cache" - {
      "must build a userAnswers object and add it to the request" - {
        "including the tax identifier when it was present in the request" in {
          val sessionRepository = mock[SessionRepository]
          val action = new Harness(sessionRepository)
          val user = aUser.copy(taxIdentifier = Some(Nino("some-nino")))

          when(sessionRepository.get(user)) thenReturn Future(Some(UserAnswers(user = user)))

          val result = action.callTransform(IdentifierRequest(user, FakeRequest())).futureValue
          result.userAnswers mustBe defined
          result.userAnswers.value.user.taxIdentifier.value mustEqual Nino("some-nino")
        }

        "not including a tax identifier when it wasn't present in the request" in {
          val sessionRepository = mock[SessionRepository]
          val action = new Harness(sessionRepository)
          val user = aUser.copy(taxIdentifier = None)

          when(sessionRepository.get(user)) thenReturn Future(Some(UserAnswers(user = user)))

          val result = action.callTransform(IdentifierRequest(user, FakeRequest())).futureValue
          result.userAnswers mustBe defined
          result.userAnswers.value.user.taxIdentifier must not be defined
        }
      }
    }
  }
}
