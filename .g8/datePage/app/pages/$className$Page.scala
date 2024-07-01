package pages

import java.time.LocalDate

import models.UserAnswers
import play.api.mvc.Call
import play.api.libs.json.JsPath

case object $className$Page extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "$className;format="decap"$"

  override def nextPageNormalMode(answers: UserAnswers): Call = ???
}
