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

import config.Service
import models.registration.requests.RegistrationRequest
import models.registration.responses.{AlreadySubscribedResponse, NoMatchResponse, RegistrationResponse}
import play.api.Configuration
import play.api.http.Status.{CONFLICT, NOT_FOUND, OK}
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(configuration: Configuration,
                                      httpClient: HttpClientV2)
                                     (implicit ec: ExecutionContext) {

  private val baseUrl = configuration.get[Service]("microservice.services.digital-platform-reporting").baseUrl

  def register(request: RegistrationRequest)(implicit hc: HeaderCarrier): Future[RegistrationResponse] =
    httpClient.post(url"$baseUrl/digital-platform-reporting/register")
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK => response.json.as[RegistrationResponse]
          case NOT_FOUND => NoMatchResponse()
          case CONFLICT => AlreadySubscribedResponse()
        }
      }
}
