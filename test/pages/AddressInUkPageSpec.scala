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

package pages

import base.SpecBase
import builders.InternationalAddressBuilder.anInternationalAddress
import builders.JerseyGuernseyIoMAddressBuilder.aJerseyGuernseyIsleOfManAddress
import builders.UkAddressBuilder.aUkAddress
import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.registration.RegisteredAddressCountry
import models.{CheckMode, NormalMode}
import org.scalatest.{OptionValues, TryValues}

class AddressInUkPageSpec extends SpecBase with TryValues with OptionValues {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Uk Address page if Uk" in {
        val answers = anEmptyAnswer.set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
        AddressInUkPage.nextPage(NormalMode, answers) mustEqual routes.UkAddressController.onPageLoad(NormalMode)
      }

      "must go to International Address page if Rest of world" in {
        val answers = anEmptyAnswer.set(AddressInUkPage, RegisteredAddressCountry.International).success.value
        AddressInUkPage.nextPage(NormalMode, answers) mustEqual routes.InternationalAddressController.onPageLoad(NormalMode)
      }

      "must go to JerseyGuernseyIoM Address page if JerseyGuernseyIoM" in {
        val answers = anEmptyAnswer.set(AddressInUkPage, RegisteredAddressCountry.JerseyGuernseyIsleOfMan).success.value
        AddressInUkPage.nextPage(NormalMode, answers) mustEqual routes.JerseyGuernseyIoMAddressController.onPageLoad(NormalMode)
      }
    }

    "in Check Mode" - {
      "must go to UK address when the answer is Uk and answers does not contain a UK address" in {
        val answers = anEmptyAnswer.set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
        AddressInUkPage.nextPage(CheckMode, answers) mustEqual routes.UkAddressController.onPageLoad(CheckMode)
      }

      "must go to International address when the answer is International and answers does not contain an International address" in {
        val answers = anEmptyAnswer.set(AddressInUkPage, RegisteredAddressCountry.International).success.value
        AddressInUkPage.nextPage(CheckMode, answers) mustEqual routes.InternationalAddressController.onPageLoad(CheckMode)
      }

      "must go to JerseyGuernseyIoM address when the answer is JGIoM and answers does not contain an JerseyGuernseyIoM address" in {
        val answers = anEmptyAnswer.set(AddressInUkPage, RegisteredAddressCountry.JerseyGuernseyIsleOfMan).success.value
        AddressInUkPage.nextPage(CheckMode, answers) mustEqual routes.JerseyGuernseyIoMAddressController.onPageLoad(CheckMode)
      }

      "must go to Check Answers" - {
        "when the answer is Uk and answers contains a UK address" in {
          val answers = anEmptyAnswer
            .set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
            .set(UkAddressPage, aUkAddress).success.value
          AddressInUkPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }

        "when the answer is International and answers contains an international address" in {
          val answers = anEmptyAnswer
            .set(AddressInUkPage, RegisteredAddressCountry.International).success.value
            .set(InternationalAddressPage, anInternationalAddress).success.value
          AddressInUkPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }

        "when the answer is JerseyGuernseyIoM and answers contains a JerseyGuernseyIoM address" in {
          val answers = anEmptyAnswer
            .set(AddressInUkPage, RegisteredAddressCountry.JerseyGuernseyIsleOfMan).success.value
            .set(JerseyGuernseyIoMAddressPage, aJerseyGuernseyIsleOfManAddress).success.value
          AddressInUkPage.nextPage(CheckMode, answers) mustEqual routes.CheckYourAnswersController.onPageLoad()
        }
      }
    }
  }

  ".cleanup" - {

    val initialAnswers = anEmptyAnswer
      .set(InternationalAddressPage, anInternationalAddress).success.value
      .set(JerseyGuernseyIoMAddressPage, aJerseyGuernseyIsleOfManAddress).success.value
      .set(UkAddressPage, aUkAddress).success.value

    "must remove International & JerseyGuernseyIoM address when the answer is Uk" in {
      val result = initialAnswers.set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value

      result.get(InternationalAddressPage) must not be defined
      result.get(JerseyGuernseyIoMAddressPage) must not be defined
      result.get(UkAddressPage) mustBe defined
    }

    "must remove UK & JerseyGeurnseyIoM address when the answer is International" in {
      val result = initialAnswers.set(AddressInUkPage, RegisteredAddressCountry.International).success.value

      result.get(UkAddressPage) must not be defined
      result.get(JerseyGuernseyIoMAddressPage) must not be defined
      result.get(InternationalAddressPage) mustBe defined
    }

    "must remove International & Uk address when the answer is JerseyGeurnseyIoM" in {
      val result = initialAnswers.set(AddressInUkPage, RegisteredAddressCountry.JerseyGuernseyIsleOfMan).success.value

      result.get(UkAddressPage) must not be defined
      result.get(InternationalAddressPage) must not be defined
      result.get(JerseyGuernseyIoMAddressPage) mustBe defined
    }
  }
}
