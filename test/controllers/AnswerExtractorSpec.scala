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

package controllers

import base.SpecBase
import builders.UserAnswersBuilder.anEmptyAnswer
import builders.UserBuilder.aUser
import models.UserAnswers
import models.requests.{IdentifierRequest, UserSessionDataRequest}
import pages.QuestionPage
import play.api.libs.json.{JsPath, Json}
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{AnyContent, Call, Result}
import play.api.test.FakeRequest
import queries.Gettable

import scala.concurrent.Future

class AnswerExtractorSpec extends SpecBase {

  private object TestPage extends QuestionPage[Int] {
    override def path: JsPath = JsPath \ "test"

    override protected def nextPageNormalMode(answers: UserAnswers): Call = Call("GET", "foo")
  }

  private def buildRequest(answers: UserAnswers): UserSessionDataRequest[AnyContent] =
    UserSessionDataRequest(answers.user, answers, IdentifierRequest(aUser, FakeRequest()))

  private class TestController extends AnswerExtractor {

    def get(query: Gettable[Int])(implicit request: UserSessionDataRequest[AnyContent]): Result =
      getAnswer(query) {
        answer => Ok(Json.toJson(answer))
      }

    def getAsync(query: Gettable[Int])(implicit request: UserSessionDataRequest[AnyContent]): Future[Result] =
      getAnswerAsync(query) {
        answer =>
          Future.successful(Ok(Json.toJson(answer)))
      }
  }

  "getAnswer" - {

    "must pass the answer into the provided block when the answer exists in user answers" in {

      val answers = anEmptyAnswer.set(TestPage, 1).success.value
      implicit val request: UserSessionDataRequest[AnyContent] = buildRequest(answers)

      val controller = new TestController()

      controller.get(TestPage) mustEqual Ok(Json.toJson(1))
    }

    "must redirect to Journey Recovery when the answer does not exist in user answers" in {

      implicit val request: UserSessionDataRequest[AnyContent] = buildRequest(anEmptyAnswer)

      val controller = new TestController()

      controller.get(TestPage) mustEqual Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }

  "getAnswerAsync" - {

    "must pass the answer into the provided block when the answer exists in user answers" in {

      val answers = anEmptyAnswer.set(TestPage, 1).success.value
      implicit val request: UserSessionDataRequest[AnyContent] = buildRequest(answers)

      val controller = new TestController()

      controller.getAsync(TestPage).futureValue mustEqual Ok(Json.toJson(1))
    }

    "must redirect to Journey Recovery when the answer does not exist in user answers" in {

      implicit val request: UserSessionDataRequest[AnyContent] = buildRequest(anEmptyAnswer)

      val controller = new TestController()

      controller.getAsync(TestPage).futureValue mustEqual Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }
}
