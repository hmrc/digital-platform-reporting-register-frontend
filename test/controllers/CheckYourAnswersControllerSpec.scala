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

import base.SpecBase
import builders.AddressBuilder
import builders.AddressBuilder.anyAddress
import builders.BusinessAddressBuilder.aBusinessAddress
import builders.UkAddressBuilder.aUkAddress
import builders.UserAnswersBuilder.aUserAnswers
import connectors.{RegistrationConnector, SubscriptionConnector}
import models.BusinessType.*
import models.audit.AuditEventModel
import models.pageviews.{CheckYourAnswersIndividualViewModel, CheckYourAnswersOrganisationViewModel}
import models.registration.Address
import models.registration.requests.{IndividualWithoutId, OrganisationWithoutId}
import models.registration.responses.{MatchResponseWithId, MatchResponseWithoutId}
import models.subscription.requests.SubscriptionRequest
import models.subscription.responses.SubscribedResponse
import models.subscription.{IndividualContact, OrganisationContact}
import models.{BusinessType, IndividualName, NormalMode, RegistrationType, SoleTraderName, SubscriptionDetails}
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
import services.AuditService
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import views.html.{CheckYourAnswersIndividualView, CheckYourAnswersOrganisationView}

import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private implicit val messages: Messages = stubMessages()

  private val anyIndividualType = Gen.oneOf(SoleTrader, Individual).sample.value
  private val anyOrganisationType = Gen.oneOf(LimitedCompany, Llp, Partnership, AssociationOrTrust).sample.value

  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockSubscriptionConnector = mock[SubscriptionConnector]
  private val mockAuditService = mock[AuditService]
  private val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockRegistrationConnector,
      mockSubscriptionConnector,
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
        val answers = emptyUserAnswers.copy(registrationResponse = Some(MatchResponseWithId("safe", anyAddress, Some(anyName))))
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
        val answers = emptyUserAnswers.copy(registrationResponse = Some(MatchResponseWithoutId("safe")))
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
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "and the registration response is a match response with Id with no organisation name" in {
          val answers = emptyUserAnswers.copy(registrationResponse = Some(MatchResponseWithId("safe", anyAddress, None)))
          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "and the registration response is not a match response with Id" in {
          val answers = emptyUserAnswers.copy(registrationResponse = Some(MatchResponseWithoutId("safe")))
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
          val answers = emptyUserAnswers
            .copy(registrationResponse = Some(registrationResponse))
            .set(RegistrationTypePage, RegistrationType.PlatformOperator).success.value
            .set(BusinessTypePage, BusinessType.SoleTrader).success.value
            .set(RegisteredInUkPage, true).success.value
            .set(IndividualEmailAddressPage, "email").success.value
            .set(CanPhoneIndividualPage, false).success.value
            .set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value

          val expectedContact = IndividualContact(models.subscription.Individual("first", "last"), "email", None)
          val expectedSubscriptionRequest = SubscriptionRequest("safeId", true, None, expectedContact, None)
          val subscriptionResponse = SubscribedResponse("dprsId")
          val subscriptionDetails = SubscriptionDetails(subscriptionResponse, expectedSubscriptionRequest, RegistrationType.PlatformOperator, Some(BusinessType.SoleTrader))
          val expectedFinalAnswers = answers.copy(data = Json.obj(), subscriptionDetails = Some(subscriptionDetails))

          when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful(subscriptionResponse))
          when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application = applicationBuilder(userAnswers = Some(answers)).overrides(
              bind[RegistrationConnector].toInstance(mockRegistrationConnector),
              bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
              bind[AuditService].toInstance(mockAuditService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
            verify(mockRegistrationConnector, never()).register(any())(any())
            verify(mockSubscriptionConnector, times(1)).subscribe(eqTo(expectedSubscriptionRequest))(any())
            verify(mockAuditService, times(1)).sendAudit(eqTo(AuditEventModel(answers)))(any())
            verify(mockSessionRepository, times(1)).set(eqTo(expectedFinalAnswers))
          }
        }
      }

      "when we already have an already subscribed registration response" - {
        "must redirect to the next page" in {
          val registrationResponse = models.registration.responses.AlreadySubscribedResponse()
          val answers = emptyUserAnswers.copy(registrationResponse = Some(registrationResponse))
          val application = applicationBuilder(userAnswers = Some(answers)).overrides(
              bind[RegistrationConnector].toInstance(mockRegistrationConnector),
              bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
              bind[AuditService].toInstance(mockAuditService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, answers).url
            verify(mockRegistrationConnector, never()).register(any())(any())
            verify(mockSubscriptionConnector, never()).subscribe(any())(any())
            verify(mockAuditService, never()).sendAudit(any())(any())
            verify(mockSessionRepository, never()).set(any())
          }
        }
      }

      "when we already have a registration no match response" - {
        "must return a failed future" in {
          val registrationResponse = models.registration.responses.NoMatchResponse()
          val answers = emptyUserAnswers.copy(registrationResponse = Some(registrationResponse))
          val application = applicationBuilder(userAnswers = Some(answers)).overrides(
              bind[RegistrationConnector].toInstance(mockRegistrationConnector),
              bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
              bind[AuditService].toInstance(mockAuditService),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            route(application, request).value.failed.futureValue

            verify(mockRegistrationConnector, never()).register(any())(any())
            verify(mockSubscriptionConnector, never()).subscribe(any())(any())
            verify(mockAuditService, never()).sendAudit(any())(any())
            verify(mockSessionRepository, never()).set(any())
          }
        }
      }

      "when we do not have a registration response" - {
        "for an individual" - {
          val aDateOfBirth = LocalDate.of(2000, 1, 2)

          "must register the user" - {
            "and submit a subscription, record it in user answers, remove the user's data, and redirect to the next page when the registration succeeds" in {
              val registrationResponse = MatchResponseWithId("safeId", anyAddress, None)
              val answers = emptyUserAnswers
                .set(BusinessTypePage, BusinessType.SoleTrader).success.value
                .set(RegisteredInUkPage, false).success.value
                .set(IndividualNamePage, IndividualName("first", "last")).success.value
                .set(DateOfBirthPage, aDateOfBirth).success.value
                .set(AddressInUkPage, true).success.value
                .set(UkAddressPage, aUkAddress).success.value
                .set(IndividualEmailAddressPage, "email").success.value
                .set(CanPhoneIndividualPage, false).success.value
                .set(SoleTraderNamePage, SoleTraderName("first", "last")).success.value

              val expectedRegistrationRequest = IndividualWithoutId("first", "last", aDateOfBirth, Address.fromUkAddress(aUkAddress))
              val expectedContact = IndividualContact(models.subscription.Individual("first", "last"), "email", None)
              val expectedSubscriptionRequest = SubscriptionRequest("safeId", false, None, expectedContact, None)
              val subscriptionResponse = SubscribedResponse("dprsId")
              val subscriptionDetails = SubscriptionDetails(subscriptionResponse, expectedSubscriptionRequest, RegistrationType.ThirdParty, Some(BusinessType.SoleTrader))
              val expectedFinalAnswers = answers.copy(
                data = Json.obj(),
                registrationResponse = Some(registrationResponse),
                subscriptionDetails = Some(subscriptionDetails)
              )

              when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))
              when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful(subscriptionResponse))
              when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
              when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

              val application = applicationBuilder(userAnswers = Some(answers))
                .overrides(
                  bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                  bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                  bind[AuditService].toInstance(mockAuditService),
                  bind[SessionRepository].toInstance(mockSessionRepository)
                )
                .build()

              running(application) {
                val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
                verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
                verify(mockSubscriptionConnector, times(1)).subscribe(eqTo(expectedSubscriptionRequest))(any())
                verify(mockAuditService, times(1)).sendAudit(eqTo(AuditEventModel(answers)))(any())
                verify(mockSessionRepository, times(1)).set(eqTo(expectedFinalAnswers))
              }
            }
          }

          "and redirect to the next page when the result is Already Subscribed" in {
            val registrationResponse = models.registration.responses.AlreadySubscribedResponse()
            val answers = emptyUserAnswers
              .set(BusinessTypePage, BusinessType.SoleTrader).success.value
              .set(RegisteredInUkPage, false).success.value
              .set(IndividualNamePage, IndividualName("first", "last")).success.value
              .set(DateOfBirthPage, aDateOfBirth).success.value
              .set(AddressInUkPage, true).success.value
              .set(UkAddressPage, aUkAddress).success.value
            val expectedRegistrationRequest = IndividualWithoutId("first", "last", aDateOfBirth, Address.fromUkAddress(aUkAddress))
            val expectedFinalAnswers = answers.copy(registrationResponse = Some(registrationResponse))

            when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))

            val application = applicationBuilder(userAnswers = Some(answers)).overrides(
                bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[AuditService].toInstance(mockAuditService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
              verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
              verify(mockSubscriptionConnector, never()).subscribe(any())(any())
              verify(mockAuditService, never()).sendAudit(any())(any())
              verify(mockSessionRepository, never()).set(any())
            }
          }

          "and return a failed future when the result is No Match (scenario should never happen in practice)" in {
            val registrationResponse = models.registration.responses.NoMatchResponse()
            val answers = emptyUserAnswers
              .set(BusinessTypePage, BusinessType.SoleTrader).success.value
              .set(RegisteredInUkPage, false).success.value
              .set(IndividualNamePage, IndividualName("first", "last")).success.value
              .set(DateOfBirthPage, aDateOfBirth).success.value
              .set(AddressInUkPage, true).success.value
              .set(UkAddressPage, aUkAddress).success.value
            val expectedRegistrationRequest = IndividualWithoutId("first", "last", aDateOfBirth, Address.fromUkAddress(aUkAddress))

            when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))

            val application = applicationBuilder(userAnswers = Some(answers)).overrides(
                bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[AuditService].toInstance(mockAuditService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
              route(application, request).value.failed.futureValue

              verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
              verify(mockSubscriptionConnector, never()).subscribe(any())(any())
              verify(mockAuditService, never()).sendAudit(any())(any())
              verify(mockSessionRepository, never()).set(any())
            }
          }
        }

        "for an organisation" - {
          "must register the user" - {
            "and submit a subscription, record it in user answers, remove the user's data, and redirect to the next page when the registration succeeds" in {
              val registrationResponse = MatchResponseWithId("safeId", anyAddress, None)
              val answers = emptyUserAnswers
                .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
                .set(RegisteredInUkPage, false).success.value
                .set(BusinessNameNoUtrPage, "name").success.value
                .set(HasBusinessTradingNamePage, false).success.value
                .set(BusinessAddressPage, aBusinessAddress).success.value
                .set(PrimaryContactNamePage, "contact name").success.value
                .set(PrimaryContactEmailAddressPage, "email").success.value
                .set(CanPhonePrimaryContactPage, false).success.value
                .set(HasSecondaryContactPage, false).success.value

              val expectedRegistrationRequest = OrganisationWithoutId("name", Address.apply(aBusinessAddress))
              val expectedContact = OrganisationContact(models.subscription.Organisation("contact name"), "email", None)
              val expectedSubscriptionRequest = SubscriptionRequest("safeId", false, None, expectedContact, None)
              val subscriptionResponse = SubscribedResponse("dprsId")
              val subscriptionDetails = SubscriptionDetails(subscriptionResponse, expectedSubscriptionRequest, RegistrationType.ThirdParty, Some(BusinessType.LimitedCompany))
              val expectedFinalAnswers = answers.copy(
                data = Json.obj(),
                registrationResponse = Some(registrationResponse),
                subscriptionDetails = Some(subscriptionDetails)
              )

              when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))
              when(mockSubscriptionConnector.subscribe(any())(any())).thenReturn(Future.successful(subscriptionResponse))
              when(mockAuditService.sendAudit(any())(any())).thenReturn(Future.successful(AuditResult.Success))
              when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

              val application =
                applicationBuilder(userAnswers = Some(answers))
                  .overrides(
                    bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                    bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                    bind[AuditService].toInstance(mockAuditService),
                    bind[SessionRepository].toInstance(mockSessionRepository)
                  )
                  .build()

              running(application) {
                val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
                val result = route(application, request).value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
                verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
                verify(mockSubscriptionConnector, times(1)).subscribe(eqTo(expectedSubscriptionRequest))(any())
                verify(mockAuditService, times(1)).sendAudit(eqTo(AuditEventModel(answers)))(any())
                verify(mockSessionRepository, times(1)).set(eqTo(expectedFinalAnswers))
              }
            }
          }

          "and redirect to the next page when the result is Already Subscribed" in {
            val registrationResponse = models.registration.responses.AlreadySubscribedResponse()
            val answers = emptyUserAnswers
              .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
              .set(RegisteredInUkPage, false).success.value
              .set(BusinessNameNoUtrPage, "name").success.value
              .set(HasBusinessTradingNamePage, false).success.value
              .set(BusinessAddressPage, aBusinessAddress).success.value
            val expectedRegistrationRequest = OrganisationWithoutId("name", Address.apply(aBusinessAddress))
            val expectedFinalAnswers = answers.copy(registrationResponse = Some(registrationResponse))

            when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))

            val application = applicationBuilder(userAnswers = Some(answers)).overrides(
                bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[AuditService].toInstance(mockAuditService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual CheckYourAnswersPage.nextPage(NormalMode, expectedFinalAnswers).url
              verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
              verify(mockSubscriptionConnector, never()).subscribe(any())(any())
              verify(mockAuditService, never()).sendAudit(any())(any())
              verify(mockSessionRepository, never()).set(any())
            }
          }

          "and return a failed future when the result is No Match (scenario should never happen in practice)" in {
            val registrationResponse = models.registration.responses.NoMatchResponse()
            val answers = emptyUserAnswers
              .set(BusinessTypePage, BusinessType.LimitedCompany).success.value
              .set(RegisteredInUkPage, false).success.value
              .set(BusinessNameNoUtrPage, "name").success.value
              .set(HasBusinessTradingNamePage, false).success.value
              .set(BusinessAddressPage, aBusinessAddress).success.value
            val expectedRegistrationRequest = OrganisationWithoutId("name", Address.apply(aBusinessAddress))

            when(mockRegistrationConnector.register(any())(any())).thenReturn(Future.successful(registrationResponse))

            val application = applicationBuilder(userAnswers = Some(answers)).overrides(
                bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                bind[SubscriptionConnector].toInstance(mockSubscriptionConnector),
                bind[AuditService].toInstance(mockAuditService),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
              route(application, request).value.failed.futureValue

              verify(mockRegistrationConnector, times(1)).register(eqTo(expectedRegistrationRequest))(any())
              verify(mockSubscriptionConnector, never()).subscribe(any())(any())
              verify(mockAuditService, never()).sendAudit(any())(any())
              verify(mockSessionRepository, never()).set(any())
            }
          }
        }
      }
    }
  }
}