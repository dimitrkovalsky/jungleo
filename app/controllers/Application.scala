package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._
import views._
import actors.{VerificationMessage, VerificationActor}
import akka.actor._
import akka.actor.{Actor, Props, Terminated}
import play.libs.Akka
import java.util.UUID
object Application extends Controller {


    def index = Action {
        Ok(html.index())
    }

    // -- Authentication
    val loginForm = Form(tuple("username" -> text(minLength = 4), "password" -> text).verifying("Invalid email or password", result => result match {
        case (username, password) => User.authenticate(username, password).isDefined
    }))

    /**
     * Login page.
     */
    def login = Action {
        implicit request =>
            val user = session.get("username").map {
                username => println(username); Some(username)
            }.getOrElse
            {
                None
            }

            Ok(html.login(loginForm, user))
    }

    /**
     * Handle login form submission.
     */
    def authenticate = Action {
        implicit request => println("[Application] authenticate : " + request)
            loginForm.bindFromRequest.fold(formWithErrors => BadRequest(html.login(formWithErrors, session.get("username"))), user => {
                Redirect(routes.Workspace.workspace(1.toString)).withSession("username" -> user._1)
            })
    }

    /**
     * Logout and clean the session.
     */
    def logout = Action {
        Redirect(routes.Application.login).withNewSession.flashing("success" -> "You've been logged out")
    }

    def error(message: String) = Action {
        implicit request =>
            Ok(html.error(message, session.get("username")))
    }
}


/**
 * Provide security features
 */
trait Secured {

    /**
     * Retrieve the connected user email.
     */
    private def username(request: RequestHeader) = request.session.get("username")

    /**
     * Redirect to login if the user in not authorized.
     */
    private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)

    // --
    /**
     * Action for authenticated users.
     */
    def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) {
        user =>
            Action(request => f(user)(request))
    }

}
