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

package models.pageviews

import builders.MatchResponseWithIdBuilder.aMatchResponseWithId
import builders.MatchResponseWithoutIdBuilder.aMatchResponseWithoutId
import builders.UserAnswersBuilder.{aUserAnswers, anEmptyAnswer}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class BusinessDetailsMatchOtherViewModelSpec extends AnyFreeSpec with Matchers {

  private val underTest = BusinessDetailsMatchOtherViewModel

  ".apply" - {
    "must return BusinessDetailsMatchOtherViewModel when userAnswers has a MatchResponseWithId" in {
      val userAnswers = aUserAnswers.copy(registrationResponse = Some(aMatchResponseWithId))
      underTest.apply(userAnswers).get mustBe
        BusinessDetailsMatchOtherViewModel(aMatchResponseWithId.organisationName.get, aMatchResponseWithId.address)
    }

    "must return None when userAnswers has no MatchResponseWithId" in {
      val userAnswers = aUserAnswers.copy(registrationResponse = Some(aMatchResponseWithoutId))
      underTest.apply(userAnswers) mustBe None
    }

    "must return None when " in {
      underTest.apply(anEmptyAnswer) mustBe None
    }
  }
}
