package controllers

import controllers.actions.*
import forms.$className$FormProvider
import models.Mode
import models.pageviews.$className$ViewModel
import pages.$className$Page
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.$className$View

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class $className$Controller @Inject()(sessionRepository: SessionRepository,
                                      identify: IdentifierActionProvider,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: $className$FormProvider,
                                      view: $className$View)
                                     (implicit mcc: MessagesControllerComponents, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val userAnswers = request.userAnswers
    Ok(view($className$ViewModel(mode, userAnswers, formProvider())))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    formProvider().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view($className$ViewModel(mode, request.userAnswers, formWithErrors)))),
      value =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set($className$Page, value))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect($className$Page.nextPage(mode, updatedAnswers))
    )
  }
}
