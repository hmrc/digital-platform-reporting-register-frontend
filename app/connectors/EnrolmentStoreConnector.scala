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
import models.eacd.requests.GroupEnrolment
import org.apache.pekko.Done
import play.api.http.Status.{CONFLICT, CREATED}
import play.api.libs.json.Json
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentStoreConnector @Inject()(appConfig: AppConfig, httpClient: HttpClientV2)
                                       (implicit ec: ExecutionContext) {

  /**
   * ES8 (admin)
   *
   * e.g. POST /enrolment-store/groups/83d19215-dddb-43bf-972d-740a11157557/enrolments/HMRC-DPRS~DPRSID~XSP1234567890
   * {
   * "userId" : "0000000021313132",
   * "type":         "principal",
   * "action" :       "enrolAndActivate"
   * }
   * Can return 409 Conflict (if enrolment already exists)
   */
  def allocateEnrolmentToGroup(groupEnrolment: GroupEnrolment)(implicit hc: HeaderCarrier): Future[Done] = {
    httpClient.post(url"${appConfig.taxEnrolmentsBaseUrl}/enrolment-store/groups/${groupEnrolment.groupId}/enrolments/${groupEnrolment.enrolmentKey}")
      .withBody(Json.toJson(groupEnrolment))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case CREATED | CONFLICT => Future.successful(Done)
        }
      }
  }
}