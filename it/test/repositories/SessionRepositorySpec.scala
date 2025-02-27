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

package repositories

import base.SpecBase
import builders.UserAnswersBuilder.aUserAnswers
import builders.UserBuilder.aUser
import models.{User, UserAnswers}
import org.mongodb.scala.model.Indexes
import org.scalactic.source.Position
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.slf4j.MDC
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import uk.gov.hmrc.play.bootstrap.dispatchers.MDCPropagatingExecutorService

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.{ExecutionContext, Future}

class SessionRepositorySpec extends SpecBase
  with DefaultPlayMongoRepositorySupport[UserAnswers]
  with ScalaFutures
  with IntegrationPatience
  with GuiceOneAppPerSuite
  with OptionValues {

  protected val config: Map[String, String] = Map(
    "mongodb.timeToLiveInSeconds" -> "900"
  )

  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  override def fakeApplication(): Application = GuiceApplicationBuilder()
    .configure(config)
    .overrides(
      bind[MongoComponent].toInstance(mongoComponent),
      bind[Clock].toInstance(stubClock)
    ).build()

  override protected val repository: SessionRepository = app.injector.instanceOf[SessionRepository]

  "Indexes" - {
    "must have the correct ttl index" in {
      val ttlIndex = repository.indexes.find(_.getOptions.getName == "lastUpdatedIdx").value
      ttlIndex.getOptions.getExpireAfter(TimeUnit.SECONDS) mustEqual 900
      ttlIndex.getKeys mustEqual Indexes.ascending("lastUpdated")
    }
  }

  ".set(...)" - {
    "must add UserAnswers if does not exist in DB" in {
      val user = User("some-id")
      val expectedResult = aUserAnswers.copy(user = user, lastUpdated = instant)

      repository.set(aUserAnswers.copy(user = user)).futureValue

      findAll().futureValue must contain only expectedResult
    }
  }

  ".get(...)" - {
    "must retrieve the right user answers" in {
      val userAnswers = aUserAnswers.copy(lastUpdated = instant)
      insert(userAnswers).futureValue
      repository.get(userAnswers.user).futureValue.value mustBe userAnswers
    }

    "must return None when the user answers does not exist" in {
      repository.get(aUser).futureValue mustBe None
    }
  }

  ".clear(...)" - {
    "must remove existing item in the DB" in {
      insert(aUserAnswers).futureValue

      repository.clear(aUserAnswers.user.id).futureValue

      findAll().futureValue mustBe empty
    }
  }

  "MDC preservation" - {
    mustPreserveMdc(repository.set(aUserAnswers))
  }

  private def mustPreserveMdc[A](f: => Future[A])(implicit pos: Position): Unit =
    "must preserve MDC" in {
      implicit lazy val ec: ExecutionContext = ExecutionContext.fromExecutor(new MDCPropagatingExecutorService(Executors.newFixedThreadPool(2)))

      MDC.put("test", "foo")

      f.map(_ => MDC.get("test") mustEqual "foo").futureValue
    }
}
