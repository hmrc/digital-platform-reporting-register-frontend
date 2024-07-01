package pages

import models.{$className$, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object $className$Page extends QuestionPage[Set[$className$]] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"

  override def nextPageNormalMode(answers: UserAnswers): Call = ???
}
