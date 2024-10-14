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
import config.ConfigKeys.*
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

  val taxEnrolmentsBaseUrl: String = configuration.get[Service](TaxEnrolmentsUrlKey).baseUrl

  val emailServiceUrl: String = configuration.get[Service](EmailServiceUrlKey).baseUrl

  private val exitSurveyBaseUrl: String = configuration.get[String]("feedback-frontend.host")
  val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/digital-platform-reporting-register-frontend"

  val auditSource: String = configuration.get[String]("auditing.auditSource")

  val digitalPlatformReportingUrl: String = configuration.get[Service](DigitalPlatformReportingUrlKey).baseUrl

  private val platformOperatorFrontendBaseUrl: String = configuration.get[Service](PlatformOperatorFrontendBaseUrlKey).baseUrl
  private val platformOperatorFrontendUrl: String = s"$platformOperatorFrontendBaseUrl/digital-platform-reporting"
  val addPlatformOperatorUrl: String = s"$platformOperatorFrontendUrl/platform-operator/add-platform-operator/start"

  private val manageFrontendBaseUrl: String = configuration.get[Service](ManageFrontendBaseUrlKey).baseUrl
  val manageFrontendUrl: String = s"$manageFrontendBaseUrl/digital-platform-reporting/manage-reporting"

  val timeout: Int = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  val dataEncryptionEnabled: Boolean = configuration.get[Boolean]("features.use-encryption")

  val isPrivateBeta: Boolean = configuration.getOptional("features.private-beta").getOrElse(false)

  val languageTranslationEnabled: Boolean = configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val userAllowListEnabled: Boolean = configuration.get[Boolean]("features.user-allow-list")

  val utrAllowListFeature = "UTR"
  val vrnAllowListFeature = "VRN"
}
