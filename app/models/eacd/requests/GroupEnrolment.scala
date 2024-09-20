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

package models.eacd.requests

import models.eacd.Identifier
import models.eacd.requests.GroupEnrolment.serviceName
import play.api.libs.json.{Json, Writes}

case class GroupEnrolment(userId: String, groupId: String, identifier: Option[Identifier] = None) {

  lazy val enrolmentKey: String = s"$serviceName~" + identifier.map(identifier => s"${identifier.key}~${identifier.value}").mkString("~")
}

object GroupEnrolment {

  private val serviceName: String = "HMRC-DPRS"

  implicit val writes: Writes[GroupEnrolment] = (o: GroupEnrolment) => Json.obj(
    "userId" -> o.userId,
    "type" -> "principal",
    "action" -> "enrolAndActivate"
  )
}
