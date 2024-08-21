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

package models

import cats.data.{EitherNec, NonEmptyChain}
import cats.implicits.*
import models.registration.responses.RegistrationResponse
import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import queries.{Gettable, Query, Settable}
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.{Failure, Success, Try}

final case class UserAnswers(id: String,
                             taxIdentifier: Option[TaxIdentifier],
                             registrationResponse: Option[RegistrationResponse] = None,
                             subscriptionDetails: Option[SubscriptionDetails] = None,
                             data: JsObject = Json.obj(),
                             lastUpdated: Instant = Instant.now) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def getEither[A](page: Gettable[A])(implicit rds: Reads[A]): EitherNec[Query, A] =
    get(page).toRight(NonEmptyChain.one(page))

  def isDefined(gettable: Gettable[_]): Boolean =
    Reads.optionNoError(Reads.at[JsValue](gettable.path)).reads(data)
      .map(_.isDefined)
      .getOrElse(false)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) => Success(jsValue)
      case JsError(errors) => Failure(JsResultException(errors))
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) => Success(jsValue)
      case JsError(_) => Success(data)
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(None, updatedAnswers)
    }
  }
}

object UserAnswers {

  def encryptedFormat(implicit crypto: Encrypter with Decrypter): OFormat[UserAnswers] = {

    implicit val sensitiveFormat: Format[SensitiveString] =
      JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)

    val encryptedReads: Reads[UserAnswers] =
      (
        (__ \ "_id").read[String] and
          (__ \ "registrationResponse").readNullable[RegistrationResponse](RegistrationResponse.encryptedFormat) and
          (__ \ "subscriptionDetails").readNullable[SubscriptionDetails](SubscriptionDetails.encryptedFormat) and
          (__ \ "data").read[SensitiveString] and
          (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
        )((id, registrationResponse, subscriptionResponse, data, lastUpdated) =>
        UserAnswers(id, None, registrationResponse, subscriptionResponse, Json.parse(data.decryptedValue).as[JsObject], lastUpdated)
      )

    val encryptedWrites: OWrites[UserAnswers] =
      (
        (__ \ "_id").write[String] and
          (__ \ "registrationResponse").writeNullable[RegistrationResponse](RegistrationResponse.encryptedFormat) and
          (__ \ "subscriptionDetails").writeNullable[SubscriptionDetails](SubscriptionDetails.encryptedFormat) and
          (__ \ "data").write[SensitiveString] and
          (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
        )(ua => (ua.id, ua.registrationResponse, ua.subscriptionDetails, SensitiveString(Json.stringify(ua.data)), ua.lastUpdated))

    OFormat(encryptedReads, encryptedWrites)
  }

  private val reads: Reads[UserAnswers] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "_id").read[String] and
        (__ \ "registrationResponse").readNullable[RegistrationResponse] and
        (__ \ "subscriptionDetails").readNullable[SubscriptionDetails] and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      )(UserAnswers.apply(_, None, _, _, _, _))
  }

  private val writes: OWrites[UserAnswers] = {

    import play.api.libs.functional.syntax.*

    (
      (__ \ "_id").write[String] and
        (__ \ "registrationResponse").writeNullable[RegistrationResponse] and
        (__ \ "subscriptionDetails").writeNullable[SubscriptionDetails] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      )(ua => (ua.id, ua.registrationResponse, ua.subscriptionDetails, ua.data, ua.lastUpdated))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}