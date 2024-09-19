package pages

import base.SpecBase
import builders.UserAnswersBuilder.anEmptyAnswer
import controllers.routes
import models.{CheckMode, NormalMode}

class $className$PageSpec extends SpecBase {

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
