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
import config.AppConfig
import connectors.RegistrationConnector
import forms.ClaimEnrolmentFormProvider
import models.eacd.{EnrolmentDetails, Identifier}
import models.BusinessType.LimitedCompany
import models.registration.Address
import models.registration.requests.{OrganisationDetails, OrganisationWithUtr}
import models.registration.responses.{MatchResponseWithId, NoMatchResponse}
import org.apache.pekko.Done
import org.scalacheck.Gen
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.EnrolmentService
import views.html.ClaimEnrolmentView

import scala.concurrent.Future

class ClaimEnrolmentControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  private val formProvider = new ClaimEnrolmentFormProvider()
  private val form = formProvider()

  private lazy val claimEnrolmentRoute = routes.ClaimEnrolmentController.onPageLoad().url

  private val utr = Gen.listOfN(10, Gen.numChar).map(_.mkString).sample.value
  private val businessName = "businessName"
  private val safeId = "safeId"
  private val dprsId = "dprsId"
  
  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockEnrolmentService = mock[EnrolmentService]

  override def beforeEach(): Unit = {
    reset(mockRegistrationConnector, mockEnrolmentService)
    super.beforeEach()
  }

  "ClaimEnrolment Controller" - {

    "for a GET" - {
      
      "must return OK and the correct view" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, claimEnrolmentRoute)

          val view = application.injector.instanceOf[ClaimEnrolmentView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form)(request, messages(application)).toString
        }
      }
    }
    
    "for a POST" - {

      "when there are no subscription identifiers in config" - {
        
        "must redirect to journey recovery" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, claimEnrolmentRoute)
                .withFormUrlEncodedBody(("utr", utr), ("businessName", businessName))
            
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }
      
      "when there are subscription identifiers in config" - {
        
        "and the user enters details that match the safe id" - {
          
          "must create an enrolment and redirect to the manage frontend" in {

            val matchResponse = MatchResponseWithId(safeId, Address("line1", None, None, None, None, "GB"), None)
            
            when(mockRegistrationConnector.register(any())(any())) thenReturn Future.successful(matchResponse)
            when(mockEnrolmentService.enrol(any())(any())) thenReturn Future.successful(Done)
            
            val application =
              applicationBuilder(userAnswers = None)
                .configure("subscriptionIdentifiers.safeId" -> safeId)
                .configure("subscriptionIdentifiers.dprsId" -> dprsId)
                .overrides(
                  bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                  bind[EnrolmentService].toInstance(mockEnrolmentService)
                )
                .build()

            running(application) {
              val request =
                FakeRequest(POST, claimEnrolmentRoute)
                  .withFormUrlEncodedBody(
                    ("utr", utr),
                    ("businessName", businessName),
                    ("businessType", LimitedCompany.toString)
                  )

              val appConfig = application.injector.instanceOf[AppConfig]
              val expectedRegisterRequest = OrganisationWithUtr(utr, Some(OrganisationDetails(businessName, LimitedCompany)))
              val expectedEnrolmentDetails = EnrolmentDetails("default-provider-id", "UTR", utr, "default-group-id", Identifier("DPRSID", dprsId))
              
              val result = route(application, request).value
              
              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual appConfig.manageFrontendUrl
              
              verify(mockRegistrationConnector).register(eqTo(expectedRegisterRequest))(any())
              verify(mockEnrolmentService).enrol(eqTo(expectedEnrolmentDetails))(any())
            }
          }
          
          "must fail when creating the enrolment fails" in {

            val matchResponse = MatchResponseWithId(safeId, Address("line1", None, None, None, None, "GB"), None)

            when(mockRegistrationConnector.register(any())(any())) thenReturn Future.successful(matchResponse)
            when(mockEnrolmentService.enrol(any())(any())) thenReturn Future.failed(RuntimeException("foo"))

            val application =
              applicationBuilder(userAnswers = None)
                .configure("subscriptionIdentifiers.safeId" -> safeId)
                .configure("subscriptionIdentifiers.dprsId" -> dprsId)
                .overrides(
                  bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                  bind[EnrolmentService].toInstance(mockEnrolmentService)
                )
                .build()

            running(application) {
              val request =
                FakeRequest(POST, claimEnrolmentRoute)
                  .withFormUrlEncodedBody(
                    ("utr", utr),
                    ("businessName", businessName),
                    ("businessType", LimitedCompany.toString)
                  )

              val expectedRegisterRequest = OrganisationWithUtr(utr, Some(OrganisationDetails(businessName, LimitedCompany)))
              val expectedEnrolmentDetails = EnrolmentDetails("default-provider-id", "UTR", utr, "default-group-id", Identifier("DPRSID", dprsId))

              route(application, request).value.failed.futureValue

              verify(mockRegistrationConnector).register(eqTo(expectedRegisterRequest))(any())
              verify(mockEnrolmentService).enrol(eqTo(expectedEnrolmentDetails))(any())
            }
          }
        }

        "and the user enters details that match a different safe id" - {

          "must redirect to journey recovery" in {

            val matchResponse = MatchResponseWithId("a different safeId", Address("line1", None, None, None, None, "GB"), None)

            when(mockRegistrationConnector.register(any())(any())) thenReturn Future.successful(matchResponse)

            val application =
              applicationBuilder(userAnswers = None)
                .configure("subscriptionIdentifiers.safeId" -> safeId)
                .configure("subscriptionIdentifiers.dprsId" -> dprsId)
                .overrides(
                  bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                  bind[EnrolmentService].toInstance(mockEnrolmentService)
                )
                .build()

            running(application) {
              val request =
                FakeRequest(POST, claimEnrolmentRoute)
                  .withFormUrlEncodedBody(
                    ("utr", utr),
                    ("businessName", businessName),
                    ("businessType", LimitedCompany.toString)
                  )

              val expectedRegisterRequest = OrganisationWithUtr(utr, Some(OrganisationDetails(businessName, LimitedCompany)))
              
              val result = route(application, request).value
              
              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

              verify(mockRegistrationConnector).register(eqTo(expectedRegisterRequest))(any())
              verify(mockEnrolmentService, never()).enrol(any())(any())
            }
          }
        }

        "and the user enters details that do not match" - {

          "must redirect to journey recovery" in {
            
            val matchResponse = NoMatchResponse()

            when(mockRegistrationConnector.register(any())(any())) thenReturn Future.successful(matchResponse)

            val application =
              applicationBuilder(userAnswers = None)
                .configure("subscriptionIdentifiers.safeId" -> safeId)
                .configure("subscriptionIdentifiers.dprsId" -> dprsId)
                .overrides(
                  bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                  bind[EnrolmentService].toInstance(mockEnrolmentService)
                )
                .build()

            running(application) {
              val request =
                FakeRequest(POST, claimEnrolmentRoute)
                  .withFormUrlEncodedBody(
                    ("utr", utr),
                    ("businessName", businessName),
                    ("businessType", LimitedCompany.toString)
                  )

              val expectedRegisterRequest = OrganisationWithUtr(utr, Some(OrganisationDetails(businessName, LimitedCompany)))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

              verify(mockRegistrationConnector).register(eqTo(expectedRegisterRequest))(any())
              verify(mockEnrolmentService, never()).enrol(any())(any())
            }
          }
        }
        
        "must fail when trying to match fails" in {
          
          when(mockRegistrationConnector.register(any())(any())) thenReturn Future.failed(RuntimeException("foo"))

          val application =
            applicationBuilder(userAnswers = None)
              .configure("subscriptionIdentifiers.safeId" -> safeId)
              .configure("subscriptionIdentifiers.dprsId" -> dprsId)
              .overrides(
                bind[RegistrationConnector].toInstance(mockRegistrationConnector),
                bind[EnrolmentService].toInstance(mockEnrolmentService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, claimEnrolmentRoute)
                .withFormUrlEncodedBody(
                  ("utr", utr),
                  ("businessName", businessName),
                  ("businessType", LimitedCompany.toString)
                )

            val expectedRegisterRequest = OrganisationWithUtr(utr, Some(OrganisationDetails(businessName, LimitedCompany)))

            route(application, request).value.failed.futureValue
            
            verify(mockRegistrationConnector).register(eqTo(expectedRegisterRequest))(any())
            verify(mockEnrolmentService, never()).enrol(any())(any())
          }
        }
        
        "and the user submits invalid data" - {

          "must return a Bad Request and errors" in {

            val application =
              applicationBuilder(userAnswers = None)
                .configure("subscriptionIdentifiers.safeId" -> safeId)
                .configure("subscriptionIdentifiers.dprsId" -> dprsId)
                .build()

            running(application) {
              val request =
                FakeRequest(POST, claimEnrolmentRoute)
                  .withFormUrlEncodedBody(("value", "invalid value"))

              val boundForm = form.bind(Map("value" -> "invalid value"))

              val view = application.injector.instanceOf[ClaimEnrolmentView]

              val result = route(application, request).value

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
            }
          }
        }
      }
    }
  }
}
