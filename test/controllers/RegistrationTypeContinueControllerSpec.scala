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

package controllers

import base.ControllerSpecBase
import builders.UserAnswersBuilder.anEmptyAnswer
import connectors.RegistrationConnector
import controllers.actions.FakeTaxIdentifierProvider
import models.registration.requests.OrganisationWithUtr
import models.registration.responses.NoMatchResponse
import models.{NormalMode, UserAnswers, Utr}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.RegistrationTypePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import scala.concurrent.Future

class RegistrationTypeContinueControllerSpec extends ControllerSpecBase with MockitoSugar {

  private lazy val registrationTypePlatformOperatorContinueRoute = routes.RegistrationTypeContinueController.platformOperator().url
  private lazy val registrationTypeThirdPartyContinueRoute = routes.RegistrationTypeContinueController.thirdParty().url

  "RegistrationTypeContinue Controller" - {

    ".platformOperator" - {

      "must store the registration response when the user has a UTR available to us, and must redirect to the next page" in {
        val mockSessionRepository = mock[SessionRepository]
        val mockConnector = mock[RegistrationConnector]
        val mockTaxIdentifierProvider = mock[FakeTaxIdentifierProvider]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockConnector.register(any())(any())).thenReturn(Future.successful(NoMatchResponse()))
        when(mockTaxIdentifierProvider.taxIdentifier).thenReturn(Some(Utr("123")))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[RegistrationConnector].toInstance(mockConnector),
            bind[FakeTaxIdentifierProvider].toInstance(mockTaxIdentifierProvider)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, registrationTypePlatformOperatorContinueRoute)
          val expectedRegistrationRequest = OrganisationWithUtr("123", None)
          val answersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual RegistrationTypePage.nextPage(NormalMode, anEmptyAnswer).url
          verify(mockConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
          verify(mockSessionRepository, times(1)).set(answersCaptor.capture())

          val savedAnswers = answersCaptor.getValue
          savedAnswers.user.taxIdentifier.value mustEqual Utr("123")
          savedAnswers.registrationResponse.value mustEqual NoMatchResponse()
        }
      }

      "must not make a registration call when the user does not have UTR available to us, and must redirect to the next page" in {
        val mockSessionRepository = mock[SessionRepository]
        val mockConnector = mock[RegistrationConnector]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(anEmptyAnswer))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[RegistrationConnector].toInstance(mockConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, registrationTypePlatformOperatorContinueRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual RegistrationTypePage.nextPage(NormalMode, anEmptyAnswer).url
          verify(mockConnector, never()).register(any())(any())
        }
      }

    }

    ".thirdParty" - {

      "must store the registration response when the user has a UTR available to us, and must redirect to the next page" in {
        val mockSessionRepository = mock[SessionRepository]
        val mockConnector = mock[RegistrationConnector]
        val mockTaxIdentifierProvider = mock[FakeTaxIdentifierProvider]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockConnector.register(any())(any())).thenReturn(Future.successful(NoMatchResponse()))
        when(mockTaxIdentifierProvider.taxIdentifier).thenReturn(Some(Utr("123")))

        val application = applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[RegistrationConnector].toInstance(mockConnector),
            bind[FakeTaxIdentifierProvider].toInstance(mockTaxIdentifierProvider)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, registrationTypeThirdPartyContinueRoute)
          val expectedRegistrationRequest = OrganisationWithUtr("123", None)
          val answersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual RegistrationTypePage.nextPage(NormalMode, anEmptyAnswer).url
          verify(mockConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
          verify(mockSessionRepository, times(1)).set(answersCaptor.capture())

          val savedAnswers = answersCaptor.getValue
          savedAnswers.user.taxIdentifier.value mustEqual Utr("123")
          savedAnswers.registrationResponse.value mustEqual NoMatchResponse()
        }
      }

      "must not make a registration call when the user does not have UTR available to us, and must redirect to the next page" in {
        val mockSessionRepository = mock[SessionRepository]
        val mockConnector = mock[RegistrationConnector]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application = applicationBuilder(userAnswers = Some(anEmptyAnswer))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[RegistrationConnector].toInstance(mockConnector)
          )
          .build()

        running(application) {
          val request = FakeRequest(GET, registrationTypeThirdPartyContinueRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual RegistrationTypePage.nextPage(NormalMode, anEmptyAnswer).url
          verify(mockConnector, never()).register(any())(any())
        }
      }

    }

  }

}
