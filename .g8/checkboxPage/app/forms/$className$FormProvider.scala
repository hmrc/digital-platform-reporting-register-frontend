package forms

import forms.mappings.Mappings
import models.$className$
import play.api.data.Form
import play.api.data.Forms.set

import javax.inject.Inject

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[Set[$className$]] = Form(
    "value" -> set(enumerable[$className$]("$className;format="decap"$.error.required")).verifying(nonEmptySet("$className;format="decap"$.error.required"))
  )
}
