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

import config.AppConfig
import models.{User, UserAnswers}
import org.mongodb.scala.SingleObservableFuture
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import play.api.libs.json.Format
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import uk.gov.hmrc.play.http.logging.Mdc

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionRepository @Inject()(mongoComponent: MongoComponent,
                                  appConfig: AppConfig,
                                  clock: Clock)
                                 (implicit ec: ExecutionContext, crypto: Encrypter & Decrypter)
  extends PlayMongoRepository[UserAnswers](
    collectionName = "user-answers",
    mongoComponent = mongoComponent,
    domainFormat = if (appConfig.dataEncryptionEnabled) UserAnswers.encryptedFormat else UserAnswers.format,
    indexes = Seq(
      IndexModel(Indexes.ascending("lastUpdated"), IndexOptions().name("lastUpdatedIdx").expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS))
    )
  ) {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byId(id: String): Bson = Filters.equal("_id", id)

  def keepAlive(id: String): Future[Boolean] = Mdc.preservingMdc {
    collection
      .updateOne(
        filter = byId(id),
        update = Updates.set("lastUpdated", Instant.now(clock)),
      )
      .toFuture()
      .map(_ => true)
  }

  def get(user: User): Future[Option[UserAnswers]] = Mdc.preservingMdc {
    val dbResponse = keepAlive(user.id)
      .flatMap(_ => collection.find(byId(user.id)).headOption())
    dbResponse.map(_.map(_.copy(user = user)))
  }

  def set(answers: UserAnswers): Future[Boolean] = Mdc.preservingMdc {
    val updatedAnswers = answers.copy(lastUpdated = Instant.now(clock))

    collection.replaceOne(
        filter = byId(updatedAnswers.user.id),
        replacement = updatedAnswers,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => true)
  }

  def clear(id: String): Future[Boolean] = Mdc.preservingMdc {
    collection
      .deleteOne(byId(id))
      .toFuture()
      .map(_ => true)
  }
}
