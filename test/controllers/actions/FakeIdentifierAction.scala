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

package controllers.actions

import builders.UserBuilder.aUser
import config.AppConfig
import models.LoginContinue.{PlatformOperator, ThirdParty}
import models.{LoginContinue, TaxIdentifier}
import models.requests.IdentifierRequest
import play.api.mvc.*
import play.api.mvc.Results.Redirect

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeIdentifierAction @Inject()(appConfig: AppConfig,
                                     bodyParsers: PlayBodyParsers,
                                     taxIdentifierProvider: FakeTaxIdentifierProvider,
                                     hasDprsEnrollment: Boolean,
                                     withDprsEnrollmentCheck: Boolean) extends IdentifierAction {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    if (withDprsEnrollmentCheck && hasDprsEnrollment) {
      Future.successful(Redirect(appConfig.manageFrontendUrl))
    } else {
      block(IdentifierRequest(aUser.copy(taxIdentifier = taxIdentifierProvider.taxIdentifier), request))
    }
  }

  override def parser: BodyParser[AnyContent] =
    bodyParsers.default

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}

class FakeTaxIdentifierProvider @Inject() {

  def taxIdentifier: Option[TaxIdentifier] = None
}

class FakeIdentifierActionProvider @Inject()(appConfig: AppConfig,
                                             bodyParser: PlayBodyParsers,
                                             taxIdentifierProvider: FakeTaxIdentifierProvider)
                                            (implicit val executionContext: ExecutionContext)
  extends IdentifierActionProvider {

  def apply(withDprsEnrollmentCheck: Boolean = true) = new FakeIdentifierAction(
    appConfig = appConfig,
    bodyParsers = bodyParser,
    taxIdentifierProvider = taxIdentifierProvider,
    hasDprsEnrollment = false,
    withDprsEnrollmentCheck = withDprsEnrollmentCheck
  )
}

class FakeIdentifierActionProviderWithDprsEnrollment @Inject()(appConfig: AppConfig,
                                                               bodyParser: PlayBodyParsers,
                                                               taxIdentifierProvider: FakeTaxIdentifierProvider)
                                                              (implicit val executionContext: ExecutionContext)
  extends IdentifierActionProvider {

  def apply(withDprsEnrollmentCheck: Boolean = true) = new FakeIdentifierAction(
    appConfig = appConfig,
    bodyParsers = bodyParser,
    taxIdentifierProvider = taxIdentifierProvider,
    hasDprsEnrollment = true,
    withDprsEnrollmentCheck = withDprsEnrollmentCheck
  )
}

class FakeIdentifierPlatformOperatorActionProvider @Inject()(appConfig: AppConfig,
                                             bodyParser: PlayBodyParsers,
                                             taxIdentifierProvider: FakeTaxIdentifierProvider)
                                            (implicit val executionContext: ExecutionContext)
  extends IdentifierPlatformOperatorActionProvider {

  def apply(withDprsEnrollmentCheck: Boolean = true, loginContinue: LoginContinue = PlatformOperator) = new FakeIdentifierAction(
    appConfig = appConfig,
    bodyParsers = bodyParser,
    taxIdentifierProvider = taxIdentifierProvider,
    hasDprsEnrollment = false,
    withDprsEnrollmentCheck = withDprsEnrollmentCheck
  )
}

class FakeIdentifierThirdPartyActionProvider @Inject()(appConfig: AppConfig,
                                                             bodyParser: PlayBodyParsers,
                                                             taxIdentifierProvider: FakeTaxIdentifierProvider)
                                                            (implicit val executionContext: ExecutionContext)
  extends IdentifierThirdPartyActionProvider {

  def apply(withDprsEnrollmentCheck: Boolean = true, loginContinue: LoginContinue = ThirdParty) = new FakeIdentifierAction(
    appConfig = appConfig,
    bodyParsers = bodyParser,
    taxIdentifierProvider = taxIdentifierProvider,
    hasDprsEnrollment = false,
    withDprsEnrollmentCheck = withDprsEnrollmentCheck
  )
}
