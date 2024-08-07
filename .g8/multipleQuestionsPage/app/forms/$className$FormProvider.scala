package forms

import forms.mappings.Mappings
import models.$className$
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[$className$] = Form(
    mapping(
      "$field1Name$" -> text("$className;format="decap"$.error.$field1Name$.required")
        .verifying(maxLength($field1MaxLength$, "$className;format="decap"$.error.$field1Name$.length")),
      "$field2Name$" -> text("$className;format="decap"$.error.$field2Name$.required")
        .verifying(maxLength($field2MaxLength$, "$className;format="decap"$.error.$field2Name$.length"))
    )($className$.apply)(x => Some((x.$field1Name$, x.$field2Name$)))
  )
}
