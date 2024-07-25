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

package forms

import forms.UtrFormProvider.{UtrFormKey, UtrRegex}
import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class UtrFormProvider @Inject() extends Mappings {

  def apply(key: String): Form[String] = Form(
    UtrFormKey -> text(s"$key.error.required")
      .transform(_.replace(" ", ""), identity)
      .verifying(regexp(UtrRegex, s"$key.error.format"))
  )
}

object UtrFormProvider {

  val UtrFormKey: String = "value"
  val UtrRegex: String = """^([kK]{0,1}\d{10})$|^(\d{10}[kK]{0,1})$|^([kK]{0,1}\d{13})$|^(\d{13}[kK]{0,1})$"""
}
