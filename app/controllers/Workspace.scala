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


/**
 * User: Dimitr
 * Date: 19.09.13
 * Time: 12:09
 */
object Workspace extends Controller with Secured {
    def workspace(strPage: String) = Action {
        implicit request => session.get("username").map {
            username => try {
                val page = Integer.parseInt(strPage)
                val verified = User.find(username).map {
                    u => u.confirmed
                }.getOrElse(false)
                Ok(views.html.workspace
                    .workspace(Some(username), TradingObject.getAllTradingObjects(page), page, verified))
            } catch {
                case _ => Results.Redirect(routes.Application.error("Invalid page number"))
            }
        }.getOrElse {
            Results.Redirect(routes.Application.login())
        }
    }

    val searchForm = Form(tuple("query" -> text, "sector" -> optional(text), "region" -> optional(text)))

    def search(strPage: String) = Action {
        implicit request => session.get("username").map {
            username => try {
                val verified = User.find(username).map {
                    u => u.confirmed
                }.getOrElse(false)
                val query = getQueryString(request)
                val page = Integer.parseInt(strPage)
                val tos =  TradingObject.getTradingObjects(Search.queryToMap(query), page)
                val pages = tos._2
                Ok(views.html.workspace
                    .workspace(Some(username),tos._1 , page,
                    verified, query,pages))
            } catch {
                case _ => Results.Redirect(routes.Application.error("Invalid page number"))
            }
        }.getOrElse {
            Results.Redirect(routes.Application.login())
        }
    }



    def findTradingObjects(query: String): List[TradingObject] = {
        TradingObject.findAll().toList.filter(tradingObject => tradingObject.stock.symbol.equalsIgnoreCase(query) ||
            tradingObject.title.toLowerCase.contains(query.toLowerCase))
    }

    private def getQueryString(request: Request[AnyContent]): String = {
        var list: List[String] = Nil
        request.getQueryString("region").map {
            regId => list = list ::: List("region=" + regId)
        }
        request.getQueryString("sector").map {
            secId => list = list ::: List("sector=" + secId)
        }
        request.getQueryString("query").map {
            query => list = list ::: List("query=" + query)
        }
        list.mkString("&")
    }

    def stock(symbol: String) = Action {
        implicit request => session.get("username").map {
            username =>
                if (!StockChart.exists(symbol))
                    Results.Redirect(routes.Application.error("Stock does not exists"))
                else
                    Ok(views.html.workspace.stock(username, symbol))
        }.getOrElse {
            Results.Redirect(routes.Application.login())
        }
    }

    def stockSocket(username: String, symbol: String) = WebSocket.using[String] {
        request =>
            val (eumerator, channel) = Concurrent.broadcast[String]
            println("Chart Connected : " + username)
            val in = Iteratee.foreach[String](println).mapDone {
                _ =>
                    println("Chart Disconnected : " + username)
                    ReaderController.unSubscribeCharts(username)
            }
            ReaderController.subscribeCharts(username, channel, symbol)
            // ReaderController.writeHistory(channel, symbol)
            (in, eumerator)
    }

    def buy(symbol: String) = IsAuthenticated {
        username => _ =>
            Portfolio.findByUsername(username) match {
                case Some(p: Portfolio) => buyStock(p, symbol, Some(username))
                case None => Forbidden
            }
    }


    private def buyStock(portfolio: Portfolio, stockSymbol: String, user: Some[String]): Result = {
        TradingObject.findStockBySymbol(stockSymbol) match {
            case None => Ok(html.error("Invalid stock", user))
            case Some(s: Stock) =>
                if (portfolio.hasAsset(s.symbol))
                    Ok(html.error("User has this asset", user))
                else {
                    portfolio.buy(Asset(s.symbol, 1000))
                    Portfolio.save(portfolio)
                    Redirect(routes.Workspace.portfolio())
                }
        }
    }

    def portfolio() = IsAuthenticated {
        username => _ =>
            Portfolio.findByUsername(username) match {
                case Some(p: Portfolio) => Ok(html.workspace.portfolio(username, p))
                case None => Forbidden("Please login")
            }
    }

    def sell(symbol: String) = IsAuthenticated {
        username => _ =>
            Portfolio.findByUsername(username) match {
                case Some(p: Portfolio) => sellStock(p, symbol, Some(username))
                case None => Forbidden("Please login")
            }
    }

    private def sellStock(portfolio: Portfolio, stockSymbol: String, user: Option[String]): Result = {
        TradingObject.findStockBySymbol(stockSymbol) match {
            case None => Ok(html.error("Invalid stock", user))
            case Some(s: Stock) =>
                if (!portfolio.hasAsset(s.symbol))
                    Ok(html.error("User hasn't this asset", user))
                else {
                    portfolio.sell(Asset(s.symbol, 1000))
                    Portfolio.save(portfolio)
                    Redirect(routes.Workspace.portfolio())
                }
        }
    }


    def feed(username: String, page: String, query: String) = WebSocket.using[String] {
        request =>
            val (eumerator, channel) = Concurrent.broadcast[String]
            println("Feed Connected : " + username)
            val in = Iteratee.foreach[String](println).mapDone {
                _ =>
                    println("Feed Disconnected : " + username)
                    ReaderController.unSubscribeStocks(username)
            }

            ReaderController.subscribeStocks(username, page, channel, query)
            (in, eumerator)
    }


}
