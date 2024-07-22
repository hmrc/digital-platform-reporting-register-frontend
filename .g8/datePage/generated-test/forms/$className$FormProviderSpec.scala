package forms

import java.time.{LocalDate, ZoneOffset}
import forms.behaviours.DateBehaviours
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class $className$FormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val underTest = new $className$FormProvider()()

  ".value" - {
    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(underTest, "value", validData)

    behave like mandatoryDateField(underTest, "value", "$className;format="decap"$.error.required.all")
  }
}
