package models.pageviews

import models.{Mode, UserAnswers}
import pages.$className$Page
import play.api.data.Form

case class $className$ViewModel(mode: Mode, form: Form[Int])

object $className$ViewModel {

  def apply(mode: Mode, userAnswers: UserAnswers, form: Form[Int]): $className$ViewModel = {
    val optAnswerValue = userAnswers.get($className$Page)

    $className$ViewModel(
      mode = mode,
      form = optAnswerValue.fold(form)(answerValue => if (form.hasErrors) form else form.fill(answerValue))
    )
  }
}