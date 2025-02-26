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
import builders.AddressBuilder
import builders.AddressBuilder.anyAddress
import builders.BusinessAddressBuilder.aBusinessAddress
import builders.ContactDetailsBuilder.aContactDetails
import builders.JerseyGuernseyIoMAddressBuilder.aJerseyGuernseyIsleOfManAddress
import builders.SubscribedResponseBuilder.aSubscribedResponse
import builders.SubscriptionDetailsBuilder.aSubscriptionDetails
import builders.UkAddressBuilder.aUkAddress
import builders.UserAnswersBuilder.{aUserAnswers, anEmptyAnswer}
import connectors.{EmailConnector, RegistrationConnector, SubscriptionConnector}
import models.BusinessType.*
import models.audit.{AuditEventModel, FailureResponseData}
import models.eacd.{EnrolmentDetails, EnrolmentKnownFacts}
import models.email.requests.SendEmailRequest
import models.pageviews.{CheckYourAnswersIndividualViewModel, CheckYourAnswersOrganisationViewModel}
import models.registration.requests.{IndividualWithoutId, OrganisationWithoutId}
import models.registration.responses.{MatchResponseWithId, MatchResponseWithoutId}
import models.registration.{Address, RegisteredAddressCountry}
import models.subscription.requests.SubscriptionRequest
import models.subscription.requests.SubscriptionRequest.BuildSubscriptionRequestFailure
import models.subscription.responses.SubscriptionResponse
import models.subscription.{IndividualContact, OrganisationContact}
import models.{BusinessType, IndividualName, NormalMode, RegistrationType, SoleTraderName, SubscriptionDetails}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{never, times, verify, when}
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.*
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.{AuditService, EnrolmentService}
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import views.html.{CheckYourAnswersIndividualView, CheckYourAnswersOrganisationView}

import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  private implicit val messages: Messages = stubMessages()

  private val anyIndividualType = Gen.oneOf(SoleTrader, Individual).sample.value
  private val anyOrganisationType = Gen.oneOf(LimitedCompany, Llp, Partnership, AssociationOrTrust).sample.value

  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockSubscriptionConnector = mock[SubscriptionConnector]
  private val mockEmailConnector = mock[EmailConnector]
  private val mockEnrolmentService = mock[EnrolmentService]
  private val mockAuditService = mock[AuditService]
  private val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockRegistrationConnector,
      mockSubscriptionConnector,
      mockEmailConnector,
      mockEnrolmentService,
      mockAuditService,
      mockSessionRepository
    )
    super.beforeEach()
  }

  "Check Your Answers Controller" - {
    "must return OK and the correct view for a GET" - {
      "for an individual" in {
        val answers = aUserAnswers.set(BusinessTypePage, anyIndividualType).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[CheckYourAnswersIndividualView]
          val viewModel = CheckYourAnswersIndividualViewModel(answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }

      "for an organisation when business type and name have been answered" in {
        val answers = aUserAnswers
          .set(BusinessNameNoUtrPage, "any-name").success.value
          .set(BusinessTypePage, anyOrganisationType).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[CheckYourAnswersOrganisationView]
          val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }

      "for an organisation when the registration response is a match with Id containing an organisation name" in {
        val anyName = "name"
        val answers = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithId("safe", anyAddress, Some(anyName))))
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[CheckYourAnswersOrganisationView]
          val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }

      "for an organisation when the registration response is a match without Id containing " in {
        val answers = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithoutId("safe")))
          .set(BusinessNameNoUtrPage, "business name").get
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[CheckYourAnswersOrganisationView]
          val viewModel = CheckYourAnswersOrganisationViewModel.apply(answers).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(viewModel)(request, messages(application)).toString
        }
      }
    }

    "must redirect to Journey Recovery for a GET" - {
      "if no existing data is found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "if business type has not been answered" - {
        "and there is no registration response" in {
          val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "and the registration response is a match response with Id with no organisation name" in {
          val answers = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithId("safe", anyAddress, None)))
          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "and the registration response is not a match response with Id" in {
          val answers = anEmptyAnswer.copy(registrationResponse = Some(MatchResponseWithoutId("safe")))
          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
    }

    "for a POST" - {
      "when we already have a registration match" - {
        "must submit a subscription, audit event, record it in user answers, remove the user's data, and redirect to the next page" in {
          val registrationResponse = MatchResponseWithId("safeId", anyAddress, None)
          val answers = anEmptyAnswer
            .copy(registrationResponse = Some(registrationResponse))
            .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
            .set(BusinessTypePage, BusinessType.SoleTrader).success.value
            .set(RegisteredInUkPage, true).success.value
            .set(IndividualEmailAddressPage, "email").success.value
            .set(CanPhoneIndividualPage, false).success.value
            .set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value
            .set(UkAddressPage, aUkAddress).success.value

          val expectedContact = IndividualContact(models.subscription.Individual("first", "last"), "email", None)
          val expectedSubscriptionRequest = SubscriptionRequest("safeId", true, None, expectedContact, None)
          val subscriptionDetails = aSubscriptionDetails.copy(subscriptionRequest = expectedSubscriptionRequest, businessType = Some(BusinessType.SoleTrader), emailSent = true)
          val expectedFinalAnswers = answers.copy(data = Json.obj(), subscriptionDetails = Some(subscriptionDetails))
          val expectedSendEmailRequest = SendEmailRequest("email", aSubscribedResponse.dprsId, "first last")

          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful(subscriptionDetails.subscriptionResponse))
          when(mockEmailConnector.send(any())(any())).thenReturn(Future.successful(true))
          when(mockEnrolmentService.enrol(any())(any())).thenReturn(Future.successful(Done))
          when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(answers)).overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[EmailConnector].toInstance(mockEmailConnector),
            bind[EnrolmentService].toInstance(mockEnrolmentService),
            bind[AuditService].toInstance(mockAuditService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
            verify(mockRegistrationConnector, never()).register(any())(any())
            verify(mockSubscriptionConnector, times(1)).subscribe(eqTo(expectedSubscriptionRequest))(any())
            verify(mockEmailConnector, times(1)).send(eqTo(expectedSendEmailRequest))(any())
            verify(mockEnrolmentService, times(1)).enrol(eqTo(EnrolmentDetails(EnrolmentKnownFacts(answers).get, aSubscribedResponse.dprsId)))(any())
            verify(mockAuditService, times(1)).sendAudit(eqTo(AuditEventModel(false, answers.data, aSubscribedResponse)))(any())
            verify(mockSessionRepository, times(1)).set(eqTo(expectedFinalAnswers))
          }
        }

        "must return a failed future when enrolment fails" in {
          val registrationResponse = MatchResponseWithId("safeId", anyAddress, None)
          val answers = anEmptyAnswer
            .copy(registrationResponse = Some(registrationResponse))
            .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
            .set(BusinessTypePage, BusinessType.SoleTrader).success.value
            .set(RegisteredInUkPage, true).success.value
            .set(IndividualEmailAddressPage, "email").success.value
            .set(CanPhoneIndividualPage, false).success.value
            .set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value
            .set(UkAddressPage, aUkAddress).success.value

          val expectedContact = IndividualContact(models.subscription.Individual("first", "last"), "email", None)
          val expectedSubscriptionRequest = SubscriptionRequest("safeId", true, None, expectedContact, None)
          val subscriptionDetails = aSubscriptionDetails.copy(subscriptionRequest = expectedSubscriptionRequest, businessType = Some(BusinessType.SoleTrader))

          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful(subscriptionDetails.subscriptionResponse))
          when(mockEnrolmentService.enrol(any())(any())).thenReturn(Future.failed(new RuntimeException("any-error")))
          when(mockEmailConnector.send(any())(any())).thenReturn(Future.successful(true))
          when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(answers)).overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[EnrolmentService].toInstance(mockEnrolmentService),
            bind[AuditService].toInstance(mockAuditService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            route(application, request).value.failed.futureValue
          }
        }

        "must redirect to Journey Recovery, when EnrolmentKnownFacts cannot be created" in {
          val registrationResponse = MatchResponseWithId("safeId", anyAddress, None)
          val answers = anEmptyAnswer
            .copy(registrationResponse = Some(registrationResponse))
            .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
            .set(BusinessTypePage, BusinessType.SoleTrader).success.value
            .set(RegisteredInUkPage, true).success.value
            .set(IndividualEmailAddressPage, "email").success.value
            .set(CanPhoneIndividualPage, false).success.value
            .set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[EmailConnector].toInstance(mockEmailConnector),
            bind[EnrolmentService].toInstance(mockEnrolmentService),
            bind[AuditService].toInstance(mockAuditService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
            verify(mockRegistrationConnector, never()).register(any())(any())
            verify(mockSubscriptionConnector, never).subscribe(any())(any())
            verify(mockEmailConnector, never()).send(any())(any())
            verify(mockEnrolmentService, never).enrol(any())(any())
            verify(mockAuditService, never).sendAudit(any())(any())
            verify(mockSessionRepository, never).set(any())
          }
        }

        "and SubscriptionRequest could not be created" in {
          val registrationResponse = MatchResponseWithId("safeId", anyAddress, None)
          val answers = anEmptyAnswer
            .copy(registrationResponse = Some(registrationResponse))
            .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
            .set(BusinessTypePage, BusinessType.SoleTrader).success.value
            .set(RegisteredInUkPage, true).success.value
            .set(CanPhoneIndividualPage, false).success.value
            .set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value
            .set(UkAddressPage, aUkAddress).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[EmailConnector].toInstance(mockEmailConnector),
            bind[EnrolmentService].toInstance(mockEnrolmentService),
            bind[AuditService].toInstance(mockAuditService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.MissingInformationController.onPageLoad().url
            verify(mockRegistrationConnector, never()).register(any())(any())
            verify(mockSubscriptionConnector, never()).subscribe(any)(any())
            verify(mockEmailConnector, never).send(any)(any())
            verify(mockEnrolmentService, never).enrol(any)(any())
            verify(mockAuditService, never).sendAudit(any)(any())
            verify(mockSessionRepository, never).set(any)
          }
        }
      }

      "when we already have an already subscribed registration response" - {
        "must redirect to the next page" in {
          val registrationResponse = models.registration.responses.AlreadySubscribedResponse()
          val answers = anEmptyAnswer.copy(registrationResponse = Some(registrationResponse))
          val application = applicationBuilder(userAnswers = Some(answers)).overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[EmailConnector].toInstance(mockEmailConnector),
            bind[EnrolmentService].toInstance(mockEnrolmentService),
            bind[AuditService].toInstance(mockAuditService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, answers).url
            verify(mockRegistrationConnector, never()).register(any())(any())
            verify(mockSubscriptionConnector, never()).subscribe(any())(any())
            verify(mockEmailConnector, never()).send(any())(any())
            verify(mockEnrolmentService, never()).enrol(any())(any())
            verify(mockAuditService, never()).sendAudit(any())(any())
            verify(mockSessionRepository, never()).set(any())
          }
        }
      }

      "when we already have a registration no match response" - {
        "must return a failed future" in {
          val registrationResponse = models.registration.responses.NoMatchResponse()
          val answers = anEmptyAnswer.copy(registrationResponse = Some(registrationResponse))
          val application = applicationBuilder(userAnswers = Some(answers)).overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[EmailConnector].toInstance(mockEmailConnector),
            bind[EnrolmentService].toInstance(mockEnrolmentService),
            bind[AuditService].toInstance(mockAuditService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

            verify(mockRegistrationConnector, never()).register(any())(any())
            verify(mockSubscriptionConnector, never()).subscribe(any())(any())
            verify(mockEmailConnector, never()).send(any())(any())
            verify(mockEnrolmentService, never()).enrol(any())(any())
            verify(mockAuditService, never()).sendAudit(any())(any())
            verify(mockSessionRepository, never()).set(any())
          }
        }
      }

      "when we do not have a registration response" - {
        "for an individual" - {
          val aDateOfBirth = LocalDate.of(2000, 1, 2)

          "must register the user" - {
            "and submit a subscription, record it in user answers, remove the user's data, audit event, send email success, and redirect to the next page when the registration succeeds" in {
              val registrationResponse = MatchResponseWithId("safeId", anyAddress, None)
              val answers = anEmptyAnswer
                .set(BusinessTypePage, BusinessType.SoleTrader).success.value
                .set(RegisteredInUkPage, false).success.value
                .set(IndividualNamePage, IndividualName("first", "last")).success.value
                .set(DateOfBirthPage, aDateOfBirth).success.value
                .set(AddressInUkPage, RegisteredAddressCountry.JerseyGuernseyIsleOfMan).success.value
                .set(JerseyGuernseyIoMAddressPage, aJerseyGuernseyIsleOfManAddress).success.value
                .set(IndividualEmailAddressPage, "some.email@example.com").success.value
                .set(CanPhoneIndividualPage, false).success.value
                .set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value

              val contactDetails = aContactDetails.copy(emailAddress = "some.email@example.com")
              val expectedRegistrationRequest = IndividualWithoutId("first", "last", aDateOfBirth, Address(aJerseyGuernseyIsleOfManAddress), contactDetails)
              val expectedContact = IndividualContact(models.subscription.Individual("first", "last"), "some.email@example.com", None)
              val expectedSubscriptionRequest = SubscriptionRequest("safeId", false, None, expectedContact, None)
              val subscriptionDetails = aSubscriptionDetails.copy(subscriptionRequest = expectedSubscriptionRequest, registrationType = RegistrationType.ThirdParty, businessType = Some(BusinessType.SoleTrader), businessName = None, emailSent = true)
              val expectedFinalAnswers = answers.copy(
                data = Json.obj(),
                registrationResponse = Some(registrationResponse),
                subscriptionDetails = Some(subscriptionDetails),
              )
              val expectedSendEmailRequest = SendEmailRequest("some.email@example.com", aSubscribedResponse.dprsId, "first last")

              when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))
              when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful(subscriptionDetails.subscriptionResponse))
              when(mockEmailConnector.send(any())(any())).thenReturn(Future.successful(true))
              when(mockEnrolmentService.enrol(any())(any())).thenReturn(Future.successful(Done))
              when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
              when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

              val application = applicationBuilder(userAnswers = Some(answers)).overrides(
                bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[EmailConnector].toInstance(mockEmailConnector),
                bind[EnrolmentService].toInstance(mockEnrolmentService),
                bind[AuditService].toInstance(mockAuditService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

              running(application) {
                val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
                verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
                verify(mockSubscriptionConnector, times(1)).subscribe(eqTo(expectedSubscriptionRequest))(any())
                verify(mockEmailConnector, times(1)).send(eqTo(expectedSendEmailRequest))(any())
                verify(mockEnrolmentService, times(1)).enrol(eqTo(EnrolmentDetails(EnrolmentKnownFacts(answers).get, aSubscribedResponse.dprsId)))(any())
                val answersWithRegistrationResponse = answers.copy(registrationResponse = Some(registrationResponse))
                verify(mockAuditService, times(1)).sendAudit(eqTo(AuditEventModel.apply(true, answersWithRegistrationResponse.data, aSubscribedResponse)))(any())
                verify(mockSessionRepository, times(1)).set(eqTo(expectedFinalAnswers))
              }
            }

            "and submit a subscription, record it in user answers, remove the user's data, audit event, send email fails, and redirect to the next page when the registration succeeds" in {
              val registrationResponse = MatchResponseWithId("safeId", anyAddress, None)
              val answers = anEmptyAnswer
                .set(BusinessTypePage, BusinessType.SoleTrader).success.value
                .set(RegisteredInUkPage, false).success.value
                .set(IndividualNamePage, IndividualName("first", "last")).success.value
                .set(DateOfBirthPage, aDateOfBirth).success.value
                .set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
                .set(UkAddressPage, aUkAddress).success.value
                .set(IndividualEmailAddressPage, "some.email@example.com").success.value
                .set(CanPhoneIndividualPage, false).success.value
                .set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value

              val contactDetails = aContactDetails.copy(emailAddress = "some.email@example.com")
              val expectedRegistrationRequest = IndividualWithoutId("first", "last", aDateOfBirth, Address(aUkAddress), contactDetails)
              val expectedContact = IndividualContact(models.subscription.Individual("first", "last"), "some.email@example.com", None)
              val expectedSubscriptionRequest = SubscriptionRequest("safeId", false, None, expectedContact, None)
              val subscriptionDetails = aSubscriptionDetails.copy(subscriptionRequest = expectedSubscriptionRequest, registrationType = RegistrationType.ThirdParty, businessType = Some(BusinessType.SoleTrader), businessName = None, emailSent = false)
              val expectedFinalAnswers = answers.copy(
                data = Json.obj(),
                registrationResponse = Some(registrationResponse),
                subscriptionDetails = Some(subscriptionDetails),
              )
              val expectedSendEmailRequest = SendEmailRequest("some.email@example.com", aSubscribedResponse.dprsId, "first last")

              when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))
              when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful(subscriptionDetails.subscriptionResponse))
              when(mockEmailConnector.send(any())(any())).thenReturn(Future.successful(false))
              when(mockEnrolmentService.enrol(any())(any())).thenReturn(Future.successful(Done))
              when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
              when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

              val application = applicationBuilder(userAnswers = Some(answers)).overrides(
                bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[EmailConnector].toInstance(mockEmailConnector),
                bind[EnrolmentService].toInstance(mockEnrolmentService),
                bind[AuditService].toInstance(mockAuditService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

              running(application) {
                val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
                verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
                verify(mockSubscriptionConnector, times(1)).subscribe(eqTo(expectedSubscriptionRequest))(any())
                verify(mockEmailConnector, times(1)).send(eqTo(expectedSendEmailRequest))(any())
                verify(mockEnrolmentService, times(1)).enrol(eqTo(EnrolmentDetails(EnrolmentKnownFacts(answers).get, aSubscribedResponse.dprsId)))(any())
                val answersWithRegistrationResponse = answers.copy(registrationResponse = Some(registrationResponse))
                verify(mockAuditService, times(1)).sendAudit(eqTo(AuditEventModel.apply(true, answersWithRegistrationResponse.data, aSubscribedResponse)))(any())
                verify(mockSessionRepository, times(1)).set(eqTo(expectedFinalAnswers))
              }
            }
          }

          "must redirect to the next page when the registration result is Already Subscribed" in {
            val registrationResponse = models.registration.responses.AlreadySubscribedResponse()
            val answers = anEmptyAnswer
              .set(BusinessTypePage, BusinessType.SoleTrader).success.value
              .set(RegisteredInUkPage, false).success.value
              .set(IndividualNamePage, IndividualName("first", "last")).success.value
              .set(DateOfBirthPage, aDateOfBirth).success.value
              .set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
              .set(UkAddressPage, aUkAddress).success.value
              .set(IndividualEmailAddressPage, aContactDetails.emailAddress).success.value
              .set(CanPhoneIndividualPage, false).success.value

            val expectedRegistrationRequest = IndividualWithoutId("first", "last", aDateOfBirth, Address(aUkAddress), aContactDetails)
            val expectedFinalAnswers = answers.copy(registrationResponse = Some(registrationResponse))

            when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))

            val application = applicationBuilder(userAnswers = Some(answers)).overrides(
              bind[RegistrationConnector].toInstance(mockRegistrationConnector),
              bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
              bind[EmailConnector].toInstance(mockEmailConnector),
              bind[EnrolmentService].toInstance(mockEnrolmentService),
              bind[AuditService].toInstance(mockAuditService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            ).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
              verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
              verify(mockSubscriptionConnector, never()).subscribe(any())(any())
              verify(mockEmailConnector, never()).send(any())(any())
              verify(mockEnrolmentService, never()).enrol(any())(any())
              verify(mockAuditService, never()).sendAudit(any())(any())
              verify(mockSessionRepository, never()).set(any())
            }
          }

          "must return a failed future when subscription result is Already Subscribed in" in {
            val registrationResponse = MatchResponseWithId("safeId", anyAddress, None)
            val answers = anEmptyAnswer
              .set(BusinessTypePage, BusinessType.SoleTrader).success.value
              .set(RegisteredInUkPage, false).success.value
              .set(IndividualNamePage, IndividualName("first", "last")).success.value
              .set(DateOfBirthPage, aDateOfBirth).success.value
              .set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
              .set(UkAddressPage, aUkAddress).success.value
              .set(IndividualEmailAddressPage, "some.email@example.com").success.value
              .set(CanPhoneIndividualPage, false).success.value
              .set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value

            val contactDetails = aContactDetails.copy(emailAddress = "some.email@example.com")
            val expectedRegistrationRequest = IndividualWithoutId("first", "last", aDateOfBirth, Address(aUkAddress), contactDetails)
            val expectedContact = IndividualContact(models.subscription.Individual("first", "last"), "some.email@example.com", None)
            val expectedSubscriptionRequest = SubscriptionRequest("safeId", false, None, expectedContact, None)
            val subscribedResponse = models.subscription.responses.AlreadySubscribedResponse()
            val subscriptionDetails = aSubscriptionDetails.copy(subscriptionResponse = subscribedResponse, registrationType = RegistrationType.ThirdParty, businessType = Some(BusinessType.SoleTrader))
            val expectedFinalAnswers = answers.copy(
              data = Json.obj(),
              registrationResponse = Some(registrationResponse),
              subscriptionDetails = Some(subscriptionDetails)
            )

            when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))
            when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful(subscriptionDetails.subscriptionResponse))
            when(mockEmailConnector.send(any())(any())).thenReturn(Future.successful(true))
            when(mockEnrolmentService.enrol(any())(any())).thenReturn(Future.successful(Done))
            when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val application = applicationBuilder(userAnswers = Some(answers)).overrides(
              bind[RegistrationConnector].toInstance(mockRegistrationConnector),
              bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
              bind[EnrolmentService].toInstance(mockEnrolmentService),
              bind[AuditService].toInstance(mockAuditService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            ).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
              verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
              verify(mockSubscriptionConnector, times(1)).subscribe(eqTo(expectedSubscriptionRequest))(any())
              verify(mockEmailConnector, never).send(any())(any())
              verify(mockEnrolmentService, never()).enrol(any())(any())
              val answersWithRegistrationResponse = answers.copy(registrationResponse = Some(registrationResponse))
              verify(mockAuditService, times(1)).sendAudit(AuditEventModel("Subscription", answersWithRegistrationResponse.data, FailureResponseData(422, any(), "Duplicate submission")))(any())
            }
          }

          "must return a failed future when the result is No Match (scenario should never happen in practice)" in {
            val registrationResponse = models.registration.responses.NoMatchResponse()
            val answers = anEmptyAnswer
              .set(BusinessTypePage, BusinessType.SoleTrader).success.value
              .set(RegisteredInUkPage, false).success.value
              .set(IndividualNamePage, IndividualName("first", "last")).success.value
              .set(DateOfBirthPage, aDateOfBirth).success.value
              .set(AddressInUkPage, RegisteredAddressCountry.Uk).success.value
              .set(UkAddressPage, aUkAddress).success.value
              .set(IndividualEmailAddressPage, aContactDetails.emailAddress).success.value
              .set(CanPhoneIndividualPage, false).success.value

            val expectedRegistrationRequest = IndividualWithoutId("first", "last", aDateOfBirth, Address(aUkAddress), aContactDetails)

            when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))

            val application = applicationBuilder(userAnswers = Some(answers)).overrides(
              bind[RegistrationConnector].toInstance(mockRegistrationConnector),
              bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
              bind[EmailConnector].toInstance(mockEmailConnector),
              bind[EnrolmentService].toInstance(mockEnrolmentService),
              bind[AuditService].toInstance(mockAuditService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            ).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
              route(application, request).value.failed.futureValue

              verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
              verify(mockSubscriptionConnector, never()).subscribe(any())(any())
              verify(mockEmailConnector, never()).send(any())(any())
              verify(mockEnrolmentService, never()).enrol(any())(any())
              verify(mockAuditService, never()).sendAudit(any())(any())
              verify(mockSessionRepository, never()).set(any())
            }
          }
        }

        "for an organisation" - {
          "must register the user" - {
            "and submit a subscription, record it in user answers, remove the user's data, audit event, send email success, and redirect to the next page when the registration succeeds" in {
              val registrationResponse = MatchResponseWithoutId("safeId")
              val answers = anEmptyAnswer
                .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
                .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
                .set(RegisteredInUkPage, false).success.value
                .set(BusinessNameNoUtrPage, "name").success.value
                .set(HasBusinessTradingNamePage, false).success.value
                .set(BusinessAddressPage, aBusinessAddress).success.value
                .set(PrimaryContactNamePage, "contact name").success.value
                .set(PrimaryContactEmailAddressPage, aContactDetails.emailAddress).success.value
                .set(CanPhonePrimaryContactPage, false).success.value
                .set(HasSecondaryContactPage, false).success.value
                .set(PrimaryContactEmailAddressPage, aContactDetails.emailAddress).success.value

              val expectedRegistrationRequest = OrganisationWithoutId("name", Address.apply(aBusinessAddress), aContactDetails)
              val expectedContact = OrganisationContact(models.subscription.Organisation("contact name"), aContactDetails.emailAddress, None)
              val expectedSubscriptionRequest = SubscriptionRequest("safeId", false, None, expectedContact, None)
              val subscriptionDetails = aSubscriptionDetails.copy(subscriptionRequest = expectedSubscriptionRequest, businessType = Some(BusinessType.LimitedCompany), businessName = Some("name"), emailSent = true)
              val expectedFinalAnswers = answers.copy(
                data = Json.obj(),
                registrationResponse = Some(registrationResponse),
                subscriptionDetails = Some(subscriptionDetails)
              )
              val expectedSendEmailRequest = SendEmailRequest(aContactDetails.emailAddress, aSubscribedResponse.dprsId, "contact name")


              when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))
              when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful(subscriptionDetails.subscriptionResponse))
              when(mockEmailConnector.send(any())(any())).thenReturn(Future.successful(true))
              when(mockEnrolmentService.enrol(any())(any())).thenReturn(Future.successful(Done))
              when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
              when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

              val application = applicationBuilder(userAnswers = Some(answers)).overrides(
                bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[EmailConnector].toInstance(mockEmailConnector),
                bind[EnrolmentService].toInstance(mockEnrolmentService),
                bind[AuditService].toInstance(mockAuditService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

              running(application) {
                val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
                verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
                verify(mockSubscriptionConnector, times(1)).subscribe(eqTo(expectedSubscriptionRequest))(any())
                verify(mockEmailConnector, times(1)).send(eqTo(expectedSendEmailRequest))(any())
                verify(mockEnrolmentService, times(1)).enrol(eqTo(EnrolmentDetails(EnrolmentKnownFacts(answers).get, aSubscribedResponse.dprsId)))(any())
                val answersWithRegistrationResponse = answers.copy(registrationResponse = Some(registrationResponse))
                verify(mockAuditService, times(1)).sendAudit(eqTo(AuditEventModel.apply(true, answersWithRegistrationResponse.data, aSubscribedResponse)))(any())
                verify(mockSessionRepository, times(1)).set(eqTo(expectedFinalAnswers))
              }
            }

            "and submit a subscription, record it in user answers, remove the user's data, audit event, send email failure, and redirect to the next page when the registration succeeds" in {
              val registrationResponse = MatchResponseWithoutId("safeId")
              val answers = anEmptyAnswer
                .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
                .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
                .set(RegisteredInUkPage, false).success.value
                .set(BusinessNameNoUtrPage, "name").success.value
                .set(HasBusinessTradingNamePage, false).success.value
                .set(BusinessAddressPage, aBusinessAddress).success.value
                .set(PrimaryContactNamePage, "contact name").success.value
                .set(PrimaryContactEmailAddressPage, aContactDetails.emailAddress).success.value
                .set(CanPhonePrimaryContactPage, false).success.value
                .set(HasSecondaryContactPage, false).success.value
                .set(PrimaryContactEmailAddressPage, aContactDetails.emailAddress).success.value

              val expectedRegistrationRequest = OrganisationWithoutId("name", Address.apply(aBusinessAddress), aContactDetails)
              val expectedContact = OrganisationContact(models.subscription.Organisation("contact name"), aContactDetails.emailAddress, None)
              val expectedSubscriptionRequest = SubscriptionRequest("safeId", false, None, expectedContact, None)
              val subscriptionDetails = aSubscriptionDetails.copy(subscriptionRequest = expectedSubscriptionRequest, businessType = Some(BusinessType.LimitedCompany), businessName = Some("name"), emailSent = false)
              val expectedFinalAnswers = answers.copy(
                data = Json.obj(),
                registrationResponse = Some(registrationResponse),
                subscriptionDetails = Some(subscriptionDetails)
              )
              val expectedSendEmailRequest = SendEmailRequest(aContactDetails.emailAddress, aSubscribedResponse.dprsId, "contact name")


              when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))
              when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful(subscriptionDetails.subscriptionResponse))
              when(mockEmailConnector.send(any())(any())).thenReturn(Future.successful(false))
              when(mockEnrolmentService.enrol(any())(any())).thenReturn(Future.successful(Done))
              when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
              when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

              val application = applicationBuilder(userAnswers = Some(answers)).overrides(
                bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[EmailConnector].toInstance(mockEmailConnector),
                bind[EnrolmentService].toInstance(mockEnrolmentService),
                bind[AuditService].toInstance(mockAuditService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              ).build()

              running(application) {
                val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
                verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
                verify(mockSubscriptionConnector, times(1)).subscribe(eqTo(expectedSubscriptionRequest))(any())
                verify(mockEmailConnector, times(1)).send(eqTo(expectedSendEmailRequest))(any())
                verify(mockEnrolmentService, times(1)).enrol(eqTo(EnrolmentDetails(EnrolmentKnownFacts(answers).get, aSubscribedResponse.dprsId)))(any())
                val answersWithRegistrationResponse = answers.copy(registrationResponse = Some(registrationResponse))
                verify(mockAuditService, times(1)).sendAudit(eqTo(AuditEventModel.apply(true, answersWithRegistrationResponse.data, aSubscribedResponse)))(any())
                verify(mockSessionRepository, times(1)).set(eqTo(expectedFinalAnswers))
              }
            }
          }

          "must redirect to the next page when the registration result is Already Subscribed" in {
            val registrationResponse = models.registration.responses.AlreadySubscribedResponse()
            val answers = anEmptyAnswer
              .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
              .set(RegisteredInUkPage, false).success.value
              .set(BusinessNameNoUtrPage, "name").success.value
              .set(HasBusinessTradingNamePage, false).success.value
              .set(BusinessAddressPage, aBusinessAddress).success.value
              .set(PrimaryContactNamePage, "name").success.value
              .set(PrimaryContactEmailAddressPage, aContactDetails.emailAddress).success.value
              .set(CanPhonePrimaryContactPage, false).success.value

            val expectedRegistrationRequest = OrganisationWithoutId("name", Address.apply(aBusinessAddress), aContactDetails)
            val expectedFinalAnswers = answers.copy(registrationResponse = Some(registrationResponse))

            when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))

            val application = applicationBuilder(userAnswers = Some(answers)).overrides(
              bind[RegistrationConnector].toInstance(mockRegistrationConnector),
              bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
              bind[EmailConnector].toInstance(mockEmailConnector),
              bind[EnrolmentService].toInstance(mockEnrolmentService),
              bind[AuditService].toInstance(mockAuditService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            ).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
              verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
              verify(mockSubscriptionConnector, never()).subscribe(any())(any())
              verify(mockEmailConnector, never()).send(any())(any())
              verify(mockEnrolmentService, never()).enrol(any())(any())
              verify(mockAuditService, never()).sendAudit(any())(any())
              verify(mockSessionRepository, never()).set(any())
            }
          }

          "must return a failed future when subscription result is Already Subscribed in" in {
            val registrationResponse = MatchResponseWithoutId("safeId")
            val answers = anEmptyAnswer
              .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
              .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
              .set(RegisteredInUkPage, false).success.value
              .set(BusinessNameNoUtrPage, "name").success.value
              .set(HasBusinessTradingNamePage, false).success.value
              .set(BusinessAddressPage, aBusinessAddress).success.value
              .set(PrimaryContactNamePage, "contact name").success.value
              .set(PrimaryContactEmailAddressPage, aContactDetails.emailAddress).success.value
              .set(CanPhonePrimaryContactPage, false).success.value
              .set(HasSecondaryContactPage, false).success.value
              .set(PrimaryContactEmailAddressPage, aContactDetails.emailAddress).success.value

            val expectedRegistrationRequest = OrganisationWithoutId("name", Address.apply(aBusinessAddress), aContactDetails)
            val expectedContact = OrganisationContact(models.subscription.Organisation("contact name"), aContactDetails.emailAddress, None)
            val expectedSubscriptionRequest = SubscriptionRequest("safeId", false, None, expectedContact, None)
            val subscribedResponse = models.subscription.responses.AlreadySubscribedResponse()
            val subscriptionDetails = aSubscriptionDetails.copy(subscriptionResponse = subscribedResponse)
            val expectedFinalAnswers = answers.copy(
              data = Json.obj(),
              registrationResponse = Some(registrationResponse),
              subscriptionDetails = Some(subscriptionDetails)
            )

            when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))
            when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful(subscribedResponse))
            when(mockEmailConnector.send(any())(any())).thenReturn(Future.successful(true))
            when(mockEnrolmentService.enrol(any())(any())).thenReturn(Future.successful(Done))
            when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
            when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

            val application = applicationBuilder(userAnswers = Some(answers)).overrides(
              bind[RegistrationConnector].toInstance(mockRegistrationConnector),
              bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
              bind[EmailConnector].toInstance(mockEmailConnector),
              bind[EnrolmentService].toInstance(mockEnrolmentService),
              bind[AuditService].toInstance(mockAuditService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            ).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
              verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
              verify(mockSubscriptionConnector, times(1)).subscribe(eqTo(expectedSubscriptionRequest))(any())
              verify(mockEmailConnector, never()).send(any())(any())
              verify(mockEnrolmentService, never()).enrol(any())(any())
              val answersWithRegistrationResponse = answers.copy(registrationResponse = Some(registrationResponse))
              verify(mockAuditService, times(1)).sendAudit(AuditEventModel("Subscription", answersWithRegistrationResponse.data, FailureResponseData(422, any(), "Duplicate submission")))(any())
            }
          }

          "must return a failed future when the result is No Match (scenario should never happen in practice)" in {
            val registrationResponse = models.registration.responses.NoMatchResponse()
            val answers = anEmptyAnswer
              .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
              .set(RegisteredInUkPage, false).success.value
              .set(BusinessNameNoUtrPage, "name").success.value
              .set(HasBusinessTradingNamePage, false).success.value
              .set(BusinessAddressPage, aBusinessAddress).success.value
              .set(PrimaryContactNamePage, "name").success.value
              .set(PrimaryContactEmailAddressPage, aContactDetails.emailAddress).success.value
              .set(CanPhonePrimaryContactPage, false).success.value

            val expectedRegistrationRequest = OrganisationWithoutId("name", Address.apply(aBusinessAddress), aContactDetails)

            when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))

            val application = applicationBuilder(userAnswers = Some(answers)).overrides(
              bind[RegistrationConnector].toInstance(mockRegistrationConnector),
              bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
              bind[EmailConnector].toInstance(mockEmailConnector),
              bind[EnrolmentService].toInstance(mockEnrolmentService),
              bind[AuditService].toInstance(mockAuditService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            ).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
              route(application, request).value.failed.futureValue

              verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
              verify(mockSubscriptionConnector, never()).subscribe(any())(any())
              verify(mockEmailConnector, never()).send(any())(any())
              verify(mockEnrolmentService, never()).enrol(any())(any())
              verify(mockAuditService, never()).sendAudit(any())(any())
              verify(mockSessionRepository, never()).set(any())
            }
          }
        }

        "and RegistrationRequest cannot be created" in {
          val answers = anEmptyAnswer
            .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
            .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
            .set(RegisteredInUkPage, false).success.value

          when(mockEmailConnector.send(any())(any())).thenReturn(Future.successful(true))
          when(mockEnrolmentService.enrol(any())(any())).thenReturn(Future.successful(Done))
          when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(answers)).overrides(
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
            bind[EmailConnector].toInstance(mockEmailConnector),
            bind[EnrolmentService].toInstance(mockEnrolmentService),
            bind[AuditService].toInstance(mockAuditService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.MissingInformationController.onPageLoad().url
            verify(mockRegistrationConnector, never).register(any)(any())
            verify(mockSubscriptionConnector, never).subscribe(any)(any())
            verify(mockEmailConnector, never()).send(any())(any())
            verify(mockEnrolmentService, never()).enrol(any())(any())
            verify(mockAuditService, never).sendAudit(any)(any())
          }
        }

        "and RegistrationResponse cannot be created" in {

          val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            val result = route(application, request).value


            status(result) mustEqual SEE_OTHER
            //redirectLocation(result).value mustEqual routes.MissingInformationController.onPageLoad().url
          }
        }


      }
    }
  }
}