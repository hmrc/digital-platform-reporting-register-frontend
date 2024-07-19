package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends BooleanFieldBehaviours {

  private val requiredKey = "$className;format="decap"$.error.required"
  private val invalidKey = "error.boolean"

  private val underTest = new $className$FormProvider()()

  ".value" - {
    val fieldName = "value"

    behave like booleanField(
      underTest,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      underTest,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
