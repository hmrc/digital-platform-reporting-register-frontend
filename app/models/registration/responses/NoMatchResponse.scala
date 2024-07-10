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

import play.api.libs.json.*

final case class NoMatchResponse() extends RegistrationResponse

object NoMatchResponse {
  
  implicit lazy val writes: OWrites[NoMatchResponse] = new OWrites[NoMatchResponse] {
    
    override def writes(o: NoMatchResponse): JsObject =
      Json.obj("result" -> "noMatch")
  }
  
  implicit lazy val reads: Reads[NoMatchResponse] =
    (__ \ "result")
      .read[String]
      .flatMap { t =>
        if (t == "noMatch") {
          Reads.pure(NoMatchResponse())
        } else {
          Reads.failed("could not read a no match result")
        }
      }
}
