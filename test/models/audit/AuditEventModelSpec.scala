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

import builders.AuditEventModelBuilder.anAuditEventModel
import builders.FailureResponseDataBuilder.aFailureResponseData
import builders.SubscribedResponseBuilder.aSubscribedResponse
import builders.SuccessResponseDataBuilder.aSuccessResponseData
import builders.UserAnswersBuilder.aUserAnswers
import connectors.SubscriptionConnector.SubscribeFailure
import models.subscription.responses.AlreadySubscribedResponse
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.{LocalDateTime, ZoneId}

class AuditEventModelSpec extends AnyFreeSpec with Matchers {

  private val underTest = AuditEventModel

  "Audit event" - {
    "must serialise correctly with success data" in {
      val auditEventModel = anAuditEventModel.copy(
        auditType = "some-audit-type",
        requestData = Json.obj("type" -> "individual", "utr" -> "123"),
        responseData = aSuccessResponseData.copy(
          processedAt = LocalDateTime.of(2001, 1, 1, 2, 30, 23),
          subscriptionId = "some-subscription-id"
        )
      )

      Json.toJson(auditEventModel) mustBe Json.obj(
        "requestData" -> Json.obj("type" -> "individual", "utr" -> "123"),
        "responseData" -> Json.obj(
          "statusCode" -> 201,
          "status" -> "success",
          "processedAt" -> "2001-01-01T02:30:23",
          "subscriptionId" -> "some-subscription-id",
        )
      )
    }

    "must serialise correctly with failure data" in {
      val auditEventModel = anAuditEventModel.copy(
        auditType = "some-audit-type",
        requestData = Json.obj("type" -> "individual", "utr" -> "123"),
        responseData = aFailureResponseData.copy(
          statusCode = 500,
          processedAt = LocalDateTime.of(2001, 1, 1, 2, 30, 23),
          reason = "some-failure-reason"
        )
      )

      Json.toJson(auditEventModel) mustBe Json.obj(
        "requestData" -> Json.obj("type" -> "individual", "utr" -> "123"),
        "responseData" -> Json.obj(
          "statusCode" -> 500,
          "status" -> "failure",
          "processedAt" -> "2001-01-01T02:30:23",
          "reason" -> "some-failure-reason"
        )
      )
    }
  }

  ".apply(userAnswers: UserAnswers, subscribeFailure: SubscribeFailure)" - {
    "must return Subscription audit event when registration response exists in answers" in {
      val answers = aUserAnswers.copy(data = Json.obj("type" -> "individual", "utr" -> "123"))

      val expected = AuditEventModel("Subscription", answers.data, aFailureResponseData.copy(statusCode = 422))
      val result = underTest.apply(false, answers.data, SubscribeFailure(422))

      result.auditType mustBe expected.auditType
      result.requestData mustBe expected.requestData
      result.responseData.asInstanceOf[FailureResponseData].statusCode mustBe 422
      result.responseData.asInstanceOf[FailureResponseData].reason mustBe "Duplicate submission"
    }

    "must return AutoSubscription audit event when registration response does not exist in answers" in {
      val answers = aUserAnswers.copy(
        registrationResponse = None,
        data = Json.obj("type" -> "individual", "utr" -> "123")
      )

      val expected = AuditEventModel("AutoSubscription", answers.data, aFailureResponseData.copy(statusCode = 500))
      val result = underTest.apply(true, answers.data, SubscribeFailure(500))

      result.auditType mustBe expected.auditType
      result.requestData mustBe expected.requestData
      result.responseData.asInstanceOf[FailureResponseData].statusCode mustBe 500
      result.responseData.asInstanceOf[FailureResponseData].reason mustBe "Error with code: 500"
    }
  }

  ".apply(userAnswers: UserAnswers, subscriptionResponse: SubscriptionResponse)" - {
    "must return Subscription audit event when registration response exists in answers and subscribed response" in {
      val answers = aUserAnswers.copy(data = Json.obj("type" -> "individual", "utr" -> "123"))
      val subscribedResponse = aSubscribedResponse.copy(dprsId = "some-dprs-id")

      underTest.apply(false, answers.data, subscribedResponse) mustBe AuditEventModel(
        "Subscription",
        answers.data,
        SuccessResponseData(LocalDateTime.ofInstant(subscribedResponse.subscribedDateTime, ZoneId.of("UTC")), "some-dprs-id")
      )
    }

    "must return AutoSubscription audit event when registration response does not exist in answers and already subscribed response" in {
      val answers = aUserAnswers.copy(
        registrationResponse = None,
        data = Json.obj("type" -> "individual", "utr" -> "123")
      )
      val expected = AuditEventModel("AutoSubscription", answers.data, aFailureResponseData.copy(statusCode = 422))
      val result = underTest.apply(true, answers.data, AlreadySubscribedResponse())

      result.auditType mustBe expected.auditType
      result.requestData mustBe expected.requestData
      result.responseData.asInstanceOf[FailureResponseData].statusCode mustBe 422
      result.responseData.asInstanceOf[FailureResponseData].reason mustBe "Duplicate submission"
    }
  }
}
