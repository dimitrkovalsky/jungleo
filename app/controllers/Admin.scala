package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._
import views._
import views.html.helper.form
import scala.Predef._
import models.UserProfile
import scala.Some
import scala.Some
import models.Stock

/**
 * User: dkovalskyi
 * Date: 26.09.13
 * Time: 17:31
 */
case class AddStock(symbol: String, title: String, regionId: Int, sectorId: Int)

object Admin extends Controller with Secured {
    val ADMIN_USERNAME = "admin"
    val ADMIN_PASSWORD = "Jungleo1234"


    val addForm = Form(tuple("symbol" -> text, "title" -> text, "regionId" -> number, "sectorId" -> number).verifying("Invalid email or password", result => result match {
        case (symbol, title, regionId, sectorId) => !TradingObject.exist(symbol)
    }))

    /**
     * Display an empty form.
     */
    def admin = Action {
        implicit request =>
            session.get("username").map {
                username => if (!isAdmin(username)) Results.Redirect(routes.Application.login())
                else
                    Ok(html.admin.admin(addForm, Some(username), TradingObject.findAll().toList))
            }.getOrElse
            {
                Results.Redirect(routes.Application.login())
            }

    }

    def add = Action {
        implicit request => println("[Admin] add stock : " + request)
            addForm.bindFromRequest.fold(formWithErrors => BadRequest(html.admin.admin(addForm, session.get("username"), TradingObject.findAll().toList)), addStock => {
                println("[Admin] add " + addStock)
                val newStock = new AddStock(addStock._1, addStock._2, addStock._3, addStock._4)
                val to: TradingObject = TradingObject(JungleoSequence.nextTradingObjectId(), newStock.title, "", newStock.regionId, newStock.sectorId, Stock(newStock.symbol))
                TradingObject.save(to)
                ReaderController.addNewTradingObject(to)
                Results.Redirect(routes.Admin.admin)
                // Ok(html.admin.admin(addForm, session.get("username"), TradingObject.findAll().toList))
            })
    }

    private def addStock(newStock: AddStock) {
        val to: TradingObject = TradingObject(JungleoSequence.nextTradingObjectId(), newStock.title, "", newStock.regionId, newStock.sectorId, Stock(newStock.symbol))
        TradingObject.save(to)
    }

    private def isAdmin(username: String): Boolean = {
        username.equals(ADMIN_USERNAME)
    }

    def delete(symbol: String) = IsAuthenticated {
        username => _ =>
            if (isAdmin(username)) {
                TradingObject.findBySymbol(symbol) match {
                    case Some(to: TradingObject) => ReaderController.remove(to); Results.Redirect(routes.Admin.admin)
                    case None => Results.Redirect(routes.Admin.admin)
                }
            } else
                Results.Redirect(routes.Application.login())
    }
}


