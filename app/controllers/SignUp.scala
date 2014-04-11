package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.mongodb.casbah.commons.Imports._
import views._
import models._
import scala.Some
import play.api.templates.Html
import actors.{VerificationActor, VerificationMessage}
import java.util.UUID
import akka.actor.{Props, ActorRef}
import play.libs.Akka

object SignUp extends Controller {
    private val mailer: ActorRef = Akka.system.actorOf(Props[VerificationActor])

    val signUpForm: Form[User] = Form(mapping("username" -> text(minLength = 4), "fullname" -> text(minLength = 4), "email" -> email, "public" -> checked(""), "password" -> tuple("main" -> text(minLength = 6), "confirm" -> text).verifying("Passwords don't match", passwords => passwords._1 == passwords._2), "profile" -> mapping("country" -> optional(text), "gender" -> optional(text), "age" -> optional(number(min = 18, max = 100)), "professional" -> optional(text))(UserProfile.apply)(UserProfile.unapply), "accept" -> checked("You must accept the conditions")) {
        (username, fullname, email, public, passwords, profile, _) => User(username, fullname, passwords._1, email, profile, public)
    }
    {
        user => Some(user.username, user.fullName, user.email, user.public, (user.password, ""), user.profile, false)
    }.verifying("This username is not available", user => !Seq("admin", "guest").contains(user.username) && !User.exists(user.username)))

    /**
     * Display an empty form.
     */
    def form = Action {
        implicit request =>
            session.get("username").map {
                username => Ok(html.signup.form(signUpForm, Some(username)))
            }.getOrElse
            {
                Ok(html.signup.form(signUpForm, None))
            }

    }


    def submit = Action {
        implicit request =>
            println("[SignUp] submit" + request)
            signUpForm.bindFromRequest.fold(// Form has errors, redisplay it
                errors => BadRequest(html.signup.form(errors, session.get("username"))), user => createUser(user, session.get("username")))
    }


    private def createUser(user: User, us: Option[String]) = {
        // TODO : Validate user saving
        User.insert(user) match {
            case Some(id: ObjectId) => Portfolio.insert(Portfolio(user.username, id))
                print("Send mail")
                val uuid = UUID.randomUUID().toString
                mailer ! VerificationMessage(user.email, uuid)
                VerificationUser.insert(VerificationUser(uuid, user.username))
            case None => html.error("Error creating account", us)
        }
        Ok(html.signup.summary(user)).withNewSession
    }

    def verification(id: String) = Action {
        println("Verification for : " + id)
        VerificationUser.find(id).map {
            verificationUser => User.find(verificationUser.username) match {
                case Some(user:User) => user.confirmed = true
                    UserDAO.save(user)
                    VerificationUser.dao.remove(verificationUser)
                    Redirect(routes.Application.login).withNewSession
                case None => Ok(views.html.error("Verification error user does not exist", None))
            }
        }.getOrElse{
            Ok(views.html.error("Verification failed user does not exists",None))
        }

    }
}