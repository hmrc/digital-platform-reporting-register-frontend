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

package base

import controllers.actions.*
import models.UserAnswers
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{OptionValues, TryValues}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest

trait ControllerSpecBase extends SpecBase
  with TryValues
  with OptionValues
  with ScalaFutures
  with IntegrationPatience {

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None,
                                   hasDprsEnrollment: Boolean = false): GuiceApplicationBuilder = {
    val identifierActionProviderBind = if (hasDprsEnrollment) {
      bind[IdentifierActionProvider].to[FakeIdentifierActionProviderWithDprsEnrollment]
    } else {
      bind[IdentifierActionProvider].to[FakeIdentifierActionProvider]
    }
    new GuiceApplicationBuilder().overrides(
      bind[DataRequiredAction].to[DataRequiredActionImpl],
      identifierActionProviderBind,
      bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
    )
  }
}
