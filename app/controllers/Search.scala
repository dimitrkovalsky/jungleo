package controllers

import play.api.data._
import play.api.data.Forms._
import views._
import play.api.mvc._
import models._
import play.api.libs.json._
import play.api.libs.iteratee._
import models.Asset
import scala.Some
import models.Stock
import play.api.mvc.Controller

/**
 * User: Dimitr
 * Date: 22.09.13
 * Time: 14:43
 */
object Search extends Controller {
    def search = Action {
        implicit request => request.getQueryString("query") match {
            case Some("") | None => Results.Redirect(routes.Application.error("Query is empty"))
            case Some(query: String) => val list = User.findLike(query)
                if(!list.isEmpty)
                    Ok(views.html.user.list(list, session.get("username")))
                else {
                    val tos = TradingObject.getTradingObjects(Search.queryToMap(query),1)
                    if(tos._1.isEmpty)
                        Results.Redirect(routes.Application.error("No search results") )
                     else {
                        val verified = session.get("username").map{username=>
                            User.find(username).map{u=>u.confirmed}.getOrElse(false)
                        }.getOrElse(false)

                        Ok(views.html.workspace.workspace(session.get("username"), tos._1, 1, verified, "query="+query,
                            tos._2))
                    }
                }

            case _ => Results.Redirect(routes.Application.error("Search error"))
        }
    }

    // TODO : portfolio None validation
    def user(userId: String) = Action {
        implicit request =>
            def show(user: User, username: Option[String]) = {
                Ok(views.html.user.profile(UserViewFullProfile(userId, user.username,user.confirmed, user.profile, Portfolio.findByUsername(user.username).get), username))
            }
            User.findById(userId) match {
                case Some(user: User) => if (!user.public) {
                    session.get("username").map {
                        username => show(user, Some(username))
                    }.getOrElse
                    {
                        Results.Redirect(routes.Application.error("Search error"))
                    }
                    Results.Redirect(routes.Application.error("User profile is private"))
                } else
                    show(user, session.get("username"))
                case _ => Results.Redirect(routes.Application.error("User not found"))
            }

    }

    def queryToMap(query: String): Map[String, String] = {
       // println("QUERY : " + query)
        val result = scala.collection.mutable.Map[String, String]()
        val params = query.split("&").toList

        for (param <- params) {
            val splited = param.split("=").toList
            if (splited.size == 2)
                result += splited(0) -> splited(1)
        }
        result.toMap
    }

    def account(username: String) = Action {
        implicit request => session.get("username").map {
            username => User.find(username) match {
                case Some(user: User) =>  Redirect(routes.Search.user(user.id.toString))
                case _ => Results.Redirect(routes.Application.error("User is not exists"))
            }
        }.getOrElse
        {
            Results.Redirect(routes.Application.error("Please sign in"))
        }
    }


}


