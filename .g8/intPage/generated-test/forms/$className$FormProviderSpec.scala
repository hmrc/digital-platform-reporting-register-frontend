package forms

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends IntFieldBehaviours {

  private val underTest = new $className$FormProvider()()

  ".value" - {
    val fieldName = "value"
    val minimum = $minimum$
    val maximum = $maximum$
    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      underTest,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      underTest,
      fieldName,
      nonNumericError = FormError(fieldName, "$className;format="decap"$.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "$className;format="decap"$.error.wholeNumber")
    )

    behave like intFieldWithRange(
      underTest,
      fieldName,
      minimum = minimum,
      maximum = maximum,
      expectedError = FormError(fieldName, "$className;format="decap"$.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      underTest,
      fieldName,
      requiredError = FormError(fieldName, "$className;format="decap"$.error.required")
    )
  }
}
