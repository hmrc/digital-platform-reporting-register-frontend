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

package models.registration.requests

import builders.BusinessAddressBuilder.aBusinessAddress
import builders.UserAnswersBuilder.{aUserAnswers, anEmptyAnswer}
import models.registration.Address
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages.{BusinessAddressPage, BusinessNameNoUtrPage}

class OrganisationWithoutIdSpec extends AnyFreeSpec
  with Matchers
  with TryValues
  with OptionValues
  with EitherValues {

  "must build from user answers when business name without UTR and business address have been answered" in {
    val answers = aUserAnswers
      .set(BusinessNameNoUtrPage, "some-business-name").success.value
      .set(BusinessAddressPage, aBusinessAddress).success.value

    val result = OrganisationWithoutId.build(answers)
    result.value mustEqual OrganisationWithoutId("some-business-name", Address(aBusinessAddress))
  }

  "must fail to build from user answers and report all errors when mandatory data is missing" in {
    val result = OrganisationWithoutId.build(anEmptyAnswer)
    result.left.value.toChain.toList must contain theSameElementsAs Seq(BusinessNameNoUtrPage, BusinessAddressPage)
  }
}