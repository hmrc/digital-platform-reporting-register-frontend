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

package config

import com.google.inject.{Inject, Singleton}
import config.ConfigKeys.{DigitalPlatformReportingUrlKey, ManageFrontendBaseUrlKey, PlatformOperatorFrontendBaseUrlKey}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader

@Singleton
class AppConfig @Inject()(configuration: Configuration) {

  val host: String = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "digital-platform-reporting-register-frontend"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  val loginUrl: String = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String = configuration.get[String]("urls.signOut")

  private lazy val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  lazy val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/digital-platform-reporting-register-frontend"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  lazy val auditSource: String = configuration.get[String]("auditing.auditSource")

  lazy val digitalPlatformReportingUrl: String = configuration.get[Service](DigitalPlatformReportingUrlKey).baseUrl

  private lazy val platformOperatorFrontendBaseUrl: String = configuration.get[Service](PlatformOperatorFrontendBaseUrlKey).baseUrl
  lazy val platformOperatorFrontendUrl: String = s"$platformOperatorFrontendBaseUrl/digital-platform-reporting"
  lazy val addPlatformOperatorUrl: String = s"$platformOperatorFrontendUrl/add-platform-operator/start"
  
  private lazy val manageFrontendBaseUrl: String = configuration.get[Service](ManageFrontendBaseUrlKey).baseUrl
  lazy val manageFrontendUrl: String = s"$manageFrontendBaseUrl/manage-digital-platform-reporting"

  val timeout: Int = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  val dataEncryptionEnabled: Boolean = configuration.get[Boolean]("features.use-encryption")

  lazy val isPrivateBeta: Boolean = configuration.getOptional("features.private-beta").getOrElse(false)
}
