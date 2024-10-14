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

package builders

import models.email.requests.SendEmailRequest

object SendEmailRequestBuilder {

  val aSendEmailRequest: SendEmailRequest = SendEmailRequest(
    to = List("default.email@example.com"),
    templateId = "default-template-id",
    parameters = Map(
      "param-1" -> "value-1",
      "param-" -> "value-2"
    )
  )
}
