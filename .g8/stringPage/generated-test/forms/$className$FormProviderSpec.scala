package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "$className;format="decap"$.error.required"
  private val lengthKey = "$className;format="decap"$.error.length"
  private val maxLength = $maxLength$

  private val underTest = new $className$FormProvider()()

  ".value" - {
    val fieldName = "value"

    behave like fieldThatBindsValidData(
      underTest,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      underTest,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      underTest,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
