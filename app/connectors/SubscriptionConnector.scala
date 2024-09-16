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

package connectors

import config.AppConfig
import connectors.SubscriptionConnector.SubscribeFailure
import models.subscription.requests.SubscriptionRequest
import models.subscription.responses.{AlreadySubscribedResponse, SubscribedResponse, SubscriptionResponse}
import play.api.http.Status.{CONFLICT, OK}
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject()(appConfig: AppConfig,
                                      httpClient: HttpClientV2)
                                     (implicit ec: ExecutionContext) {

  def subscribe(request: SubscriptionRequest)(implicit hc: HeaderCarrier): Future[SubscriptionResponse] =
    httpClient.post(url"${appConfig.digitalPlatformReportingUrl}/digital-platform-reporting/subscribe")
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK => Future.successful(response.json.as[SubscribedResponse])
          case CONFLICT => Future.successful(AlreadySubscribedResponse())
          case status => Future.failed(SubscribeFailure(status))
        }
      }
}

object SubscriptionConnector {
  final case class SubscribeFailure(statusCode: Int) extends Throwable {
    override def getMessage(): String = s"Error with code: $statusCode"
  }
}
