package models.pageviews

import base.SpecBase
import builders.UserAnswersBuilder.aUserAnswers
import forms.$className$FormProvider
import models.{$className$, NormalMode}
import pages.$className$Page

class $className$ViewModelSpec extends SpecBase {

  private val anyMode = NormalMode
  private val formProvider = new $className$FormProvider()

  private val underTest = $className$ViewModel

  ".apply(...)" - {
    "must return ViewModel with pre-filled form when $className$Page answer available" in {
      val form = formProvider()
      val userAnswers = aUserAnswers.set($className$Page, $className$("value 1", "value 2")).get

      underTest.apply(anyMode, userAnswers, form) mustBe
        $className$ViewModel(mode = anyMode, form = form.fill($className$("value 1", "value 2")))
    }

    "must return ViewModel without pre-filled form when $className$Page answer not available" in {
      val emptyForm = formProvider()
      val userAnswers = aUserAnswers.remove($className$Page).get

      underTest.apply(anyMode, userAnswers, emptyForm) mustBe
        $className$ViewModel(mode = anyMode, form = emptyForm)
    }

    "must return ViewModel with pre-filled form with errors, when the form has errors" in {
      val formWithErrors = formProvider().bind(Map($className$Page.toString -> "unknown-value"))
      val userAnswers = aUserAnswers.remove($className$Page).get

      underTest.apply(anyMode, userAnswers, formWithErrors) mustBe
        $className$ViewModel(mode = anyMode, form = formWithErrors)
    }
  }
}
