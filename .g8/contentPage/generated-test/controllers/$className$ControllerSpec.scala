package controllers

import base.ControllerSpecBase
import builders.UserAnswersBuilder.anEmptyAnswer
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.$className$View

class $className$ControllerSpec extends ControllerSpecBase {

  "$className$ Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(anEmptyAnswer)).build()

      running(application) {
        val request = FakeRequest(GET, routes.$className$Controller.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
