/*
 * Copyright 2023 HM Revenue & Customs
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

import cats.implicits.*
import connectors.SubscriptionConnector.SubscribeFailure
import models.subscription.responses.{AlreadySubscribedResponse, SubscribedResponse, SubscriptionResponse}
import play.api.libs.json.*

import java.time.{Instant, LocalDateTime, ZoneId}

case class AuditEventModel(auditType: String, requestData: JsObject, responseData: ResponseData)

object AuditEventModel {

  implicit lazy val writes: OWrites[AuditEventModel] = (o: AuditEventModel) =>
    o.requestData + ("outcome" -> Json.toJson(o.responseData))


  def apply(isAutoSubscription: Boolean, requestData: JsObject, subscribeFailure: SubscribeFailure): AuditEventModel = {
    val localDateTime = LocalDateTime.ofInstant(Instant.now, ZoneId.of("UTC"))
    val responseData = subscribeFailure.statusCode match {
      case 422 => FailureResponseData(422, localDateTime, "Duplicate submission")
      case _ => FailureResponseData(subscribeFailure.statusCode, localDateTime, subscribeFailure.getMessage())
    }

    if isAutoSubscription then
      AuditEventModel("AutoSubscription", requestData, responseData)
    else
      AuditEventModel("Subscription", requestData, responseData)
  }

  def apply(isAutoSubscription: Boolean, requestData: JsObject, subscriptionResponse: SubscriptionResponse): AuditEventModel = {
    val responseData = subscriptionResponse match {
      case SubscribedResponse(dprsId, subscribedDateTime) => SuccessResponseData(LocalDateTime.ofInstant(subscribedDateTime, ZoneId.of("UTC")), dprsId)
      case AlreadySubscribedResponse() => FailureResponseData(422, LocalDateTime.ofInstant(Instant.now, ZoneId.of("UTC")), "Duplicate submission")
    }

    if isAutoSubscription then
      AuditEventModel("AutoSubscription", requestData, responseData)
    else
      AuditEventModel("Subscription", requestData, responseData)
  }
}