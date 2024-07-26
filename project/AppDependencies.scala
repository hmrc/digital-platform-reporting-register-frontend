import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.1.0"
  private val hmrcMongoVersion = "2.2.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"                   %% "play-frontend-hmrc-play-30" % "10.5.0",
    "uk.gov.hmrc"                   %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo"             %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "org.typelevel"                 %% "cats-core"                  % "2.12.0",
    "com.googlecode.libphonenumber"  % "libphonenumber"             % "8.13.41"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatestplus"       %% "scalacheck-1-17"         % "3.2.18.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
