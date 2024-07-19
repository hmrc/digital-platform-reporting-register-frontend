package pages

import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.{CheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class $className$PageSpec extends AnyFreeSpec with Matchers {

  ".nextPage" - {
    "in Normal Mode" - {
      "must go to Index" in {
        $className$Page.nextPage(NormalMode, anEmptyAnswer) mustEqual routes.IndexController.onPageLoad()
      }
    }

    "in Check Mode" - {
      "must go to Check Answers" in {
        $className$Page.nextPage(CheckMode, anEmptyAnswer) mustEqual routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
