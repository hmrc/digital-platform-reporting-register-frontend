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

package services

import base.SpecBase
import builders.EnrolmentDetailsBuilder.anEnrolmentDetails
import connectors.{PendingEnrolmentConnector, TaxEnrolmentConnector}
import models.eacd.requests.{GroupEnrolment, UpsertKnownFacts}
import models.enrolment.requests.PendingEnrolmentRequest
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EnrolmentServiceSpec extends SpecBase
  with ScalaFutures
  with MockitoSugar
  with FutureAwaits
  with DefaultAwaitTimeout
  with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    Mockito.reset(mockTaxEnrolmentConnector, mockPendingEnrolmentConnector)
    super.beforeEach()
  }

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val mockTaxEnrolmentConnector = mock[TaxEnrolmentConnector]
  private val mockPendingEnrolmentConnector = mock[PendingEnrolmentConnector]

  private val underTest = EnrolmentService(mockTaxEnrolmentConnector, mockPendingEnrolmentConnector)

  ".enrol(...)" - {
    "must upsert and allocate enrolment to a group" in {
      when(mockTaxEnrolmentConnector.upsert(UpsertKnownFacts(anEnrolmentDetails))).thenReturn(Future.successful(Done))
      when(mockTaxEnrolmentConnector.allocateEnrolmentToGroup(GroupEnrolment(anEnrolmentDetails))).thenReturn(Future.successful(Done))

      underTest.enrol(anEnrolmentDetails).futureValue

      verify(mockPendingEnrolmentConnector, never()).save(any())(any())
    }

    "must call save pending enrolment when when upsert fails" in {
      when(mockTaxEnrolmentConnector.upsert(UpsertKnownFacts(anEnrolmentDetails))).thenReturn(Future.failed(new RuntimeException()))
      when(mockPendingEnrolmentConnector.save(PendingEnrolmentRequest(anEnrolmentDetails))).thenReturn(Future.successful(Done))

      underTest.enrol(anEnrolmentDetails).futureValue

      verify(mockTaxEnrolmentConnector, never()).allocateEnrolmentToGroup(any())(any())
      verify(mockPendingEnrolmentConnector, times(1)).save(any())(any())
    }

    "must call save pending enrolment when allocation fails" in {
      when(mockTaxEnrolmentConnector.upsert(UpsertKnownFacts(anEnrolmentDetails))).thenReturn(Future.successful(Done))
      when(mockTaxEnrolmentConnector.allocateEnrolmentToGroup(GroupEnrolment(anEnrolmentDetails))).thenReturn(Future.failed(new RuntimeException()))
      when(mockPendingEnrolmentConnector.save(PendingEnrolmentRequest(anEnrolmentDetails))).thenReturn(Future.successful(Done))

      underTest.enrol(anEnrolmentDetails).futureValue

      verify(mockPendingEnrolmentConnector, times(1)).save(any())(any())
    }
  }
}
