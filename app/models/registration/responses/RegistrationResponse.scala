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

package models.registration.responses

import play.api.libs.functional.syntax.*
import play.api.libs.json.{JsObject, Json, OWrites, Reads}

trait RegistrationResponse

object RegistrationResponse {
  implicit lazy val reads: Reads[RegistrationResponse] =
    MatchResponseWithId.format.widen or
      MatchResponseWithoutId.format.widen or
      NoMatchResponse.reads.widen or
      AlreadySubscribedResponse.reads.widen

  implicit lazy val writes: OWrites[RegistrationResponse] = new OWrites[RegistrationResponse] {
    override def writes(o: RegistrationResponse): JsObject =
      o match {
        case x: MatchResponseWithId => Json.toJsObject(x)(MatchResponseWithId.format)
        case x: MatchResponseWithoutId => Json.toJsObject(x)(MatchResponseWithoutId.format)
        case x: NoMatchResponse => Json.toJsObject(x)(NoMatchResponse.writes)
        case x: AlreadySubscribedResponse => Json.toJsObject(x)(AlreadySubscribedResponse.writes)
      }
  }
}
