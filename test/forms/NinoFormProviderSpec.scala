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

import forms.behaviours.StringFieldBehaviours
import forms.common.Validation
import org.scalacheck.Gen
import org.scalacheck.Gen.{alphaUpperChar, choose, chooseNum, listOfN, numChar}
import play.api.data.FormError

class NinoFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "nino.error.required"
  val invalidKey = "nino.error.invalid"

  private val provider = new NinoFormProvider()
  private val form = provider()

  private val invalidChars = "DFIQUVO"

  private def validNino(): Gen[String] = for {
    spaces <- listOfN(chooseNum(1, 10).sample.getOrElse(0), choose[Char](' ', ' '))
    letters <- listOfN(2, alphaUpperChar suchThat (!invalidChars.contains(_)))
    numbers <- listOfN(6, numChar)
    letter <- listOfN(1, alphaUpperChar suchThat (c => c >= 'A' && (c <= 'D')))
  } yield Seq(spaces, letters, spaces, numbers, spaces, letter, spaces).flatten.mkString

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validNino()
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      stringsThatDoNotMatchRegex(Validation.ninoPattern),
      invalidError = FormError(fieldName, invalidKey, Seq(Validation.ninoPattern.regex))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
