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
import builders.UserBuilder.aUser
import connectors.RegistrationConnector
import forms.DateOfBirthFormProvider
import generators.ModelGenerators
import models.{IndividualName, Nino, NormalMode, UserAnswers}
import models.registration.responses.MatchResponseWithId
import models.registration.Address
import models.registration.requests.{IndividualDetails, IndividualWithNino}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{DateOfBirthPage, IndividualNamePage, NinoPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.DateOfBirthView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class DateOfBirthControllerSpec extends ControllerSpecBase with MockitoSugar with ModelGenerators {

  private implicit val messages: Messages = stubMessages()

  private val formProvider = new DateOfBirthFormProvider()

  private def form = formProvider()

  val validAnswer = LocalDate.now(ZoneOffset.UTC)

  lazy val dateOfBirthRoute = routes.DateOfBirthController.onPageLoad(NormalMode).url

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateOfBirthRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateOfBirthRoute)
      .withFormUrlEncodedBody(
        "value.day" -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year" -> validAnswer.getYear.toString
      )

  "DateOfBirth Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val result = route(application, getRequest()).value
        val view = application.injector.instanceOf[DateOfBirthView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(getRequest(), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = anEmptyAnswer.set(DateOfBirthPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[DateOfBirthView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode)(getRequest(), messages(application)).toString
      }
    }
    
    "must register the user, save answers, and redirect to the next page when the user signed in with CL 250" in {

      val nino = arbitraryNino.sample.value
      val address = Address("line 1", None, None, None, None, "GB")
      val individualDetails = IndividualDetails("first", "last")
      val registrationResponse = MatchResponseWithId("id", address, None)
      val answers =
        anEmptyAnswer
          .copy(user = aUser.copy(taxIdentifier = Some(Nino(nino))))
          .set(IndividualNamePage, IndividualName("first", "last")).success.value

      val mockSessionRepository = mock[SessionRepository]
      val mockConnector = mock[RegistrationConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockConnector.register(any())(any())) thenReturn Future.successful(registrationResponse)

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[RegistrationConnector].toInstance(mockConnector)
          )
          .build()

      running(application) {
        val expectedRegistrationRequest = IndividualWithNino(nino, individualDetails, validAnswer)
        val answersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        val finalAnswers =
          answers
            .copy(registrationResponse = Some(registrationResponse))
            .set(DateOfBirthPage, validAnswer).success.value

        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual DateOfBirthPage.nextPage(NormalMode, finalAnswers).url

        verify(mockSessionRepository, times(1)).set(answersCaptor.capture())
        verify(mockConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())

        val savedAnswers = answersCaptor.getValue
        savedAnswers.registrationResponse.value mustEqual registrationResponse
      }
    }
    
    "must register the user, save answers, and redirect to the next page when the user provided a NINO" in {

      val nino = arbitraryNino.sample.value
      val address = Address("line 1", None, None, None, None, "GB")
      val individualDetails = IndividualDetails("first", "last")
      val registrationResponse = MatchResponseWithId("id", address, None)
      val answers =
        anEmptyAnswer
          .set(NinoPage, nino).success.value
          .set(IndividualNamePage, IndividualName("first", "last")).success.value
      
      val mockSessionRepository = mock[SessionRepository]
      val mockConnector = mock[RegistrationConnector]
      
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockConnector.register(any())(any())) thenReturn Future.successful(registrationResponse)

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[RegistrationConnector].toInstance(mockConnector)
          )
          .build()

      running(application) {
        val expectedRegistrationRequest = IndividualWithNino(nino, individualDetails, validAnswer)
        val answersCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        val finalAnswers =
          answers
            .copy(registrationResponse = Some(registrationResponse))
            .set(DateOfBirthPage, validAnswer).success.value
        
        val result = route(application, postRequest()).value
        
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual DateOfBirthPage.nextPage(NormalMode, finalAnswers).url
        
        verify(mockSessionRepository, times(1)).set(answersCaptor.capture())
        verify(mockConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
        
        val savedAnswers = answersCaptor.getValue
        savedAnswers.registrationResponse.value mustEqual registrationResponse
      }
    }
    
    "must redirect to the next page when valid data is submitted and the user has not provided a NINO" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(anEmptyAnswer))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual DateOfBirthPage.nextPage(NormalMode, anEmptyAnswer).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()
      val request = FakeRequest(POST, dateOfBirthRoute)
        .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view = application.injector.instanceOf[DateOfBirthView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}