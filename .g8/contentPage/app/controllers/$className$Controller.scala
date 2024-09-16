package controllers

import controllers.actions.*
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.$className$View

import javax.inject.Inject

class $className$Controller @Inject()(identify: IdentifierActionProvider,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      view: $className$View)
                                     (implicit mcc: MessagesControllerComponents)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    Ok(view())
  }
}
