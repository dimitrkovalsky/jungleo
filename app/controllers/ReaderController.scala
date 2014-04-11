package controllers

import java.util.concurrent.TimeUnit
import com.jungleo.fetcher.StockFetcher
import models.{StockChart, TradingObject, Stock}
import scala.concurrent.duration._

import play.api.libs.json._
import play.api.libs.concurrent._


import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.Concurrent.Channel
import com.jungleo.readers.YahooFinanceReader
import akka.actor.FSM.->

/**
 * User: dkovalskyi
 * Date: 18.09.13
 * Time: 18:23
 */
case class StockClient(symbols: List[String], channel: Channel[String])

case class ChartClient(symbol: String, channel: Channel[String])

object ReaderController {

    private final val STOCK_UPDATE_MESSAGE = 1
    private final val CHART_UPDATE_MESSAGE = 2
    private final val HISTORY_MESSAGE = 3
    private final val POLL_INTERVAL = 2
    private val stockSubscribers = scala.collection.mutable.Map[String, StockClient]()
    private val chartSubscribers = scala.collection.mutable.Map[String, ChartClient]()
    var tos: scala.collection.mutable.Map[String, TradingObject] = null

    def remove(tradingObject: TradingObject) {
        // println("[ReaderController] delete")
        tos.remove(tradingObject.stock.symbol).map {
            to =>
                StockChart.findBySymbol(to.stock.symbol).map {
                    schart => StockChart.remove(schart); TradingObject.remove(tradingObject)
                }
        }
    }


    def init(random: Boolean) {
        initYahooFinance(random)
    }

    private def initYahooFinance(random: Boolean) {
        println("[ReaderController] initialization")
        tos = StockFetcher.initTradingObjects()
        val reader = new YahooFinanceReader(tos, random)
        Akka.system.scheduler.schedule(initialDelay = Duration(1, TimeUnit.SECONDS),
            interval = Duration(POLL_INTERVAL, TimeUnit.SECONDS), reader)
    }

    def addNewTradingObject(to: TradingObject) {
        tos += to.stock.symbol -> to
        var history: List[Double] = Nil
        for (i <- 1 to 50)
            history = history ::: List(0d)

        val chart = StockChart(to.stock.symbol, history)
        StockChart.save(chart)
    }

    def subscribeStocks(username: String, currentPage: String, channel: Channel[String], query: String) {
        try {
            val page = Integer.parseInt(currentPage)
            val symbols = if (query.isEmpty) {
                TradingObject.getAllTradingObjects(page).map(_.stock.symbol)
            } else {
                TradingObject.getTradingObjects(Search.queryToMap(query), page)._1.map(_.stock.symbol)
            }
            stockSubscribers += username -> StockClient(symbols, channel)
        } catch {
            case t: Throwable => println(t.getMessage)
        }
    }

    def unSubscribeStocks(username: String) {
        stockSubscribers.remove(username).map {
            subscriber => subscriber.channel.end()
        }
    }

    def writeHistory(channel: Channel[String], symbol: String): Unit = {
        StockChart.findBySymbol(symbol) match {
            case Some(chart: StockChart) => val msg = JsObject(
                Seq("message_type" -> JsNumber(HISTORY_MESSAGE), "symbol" -> JsString(symbol),
                    "history" -> JsArray(toSeq(chart.history))))
                println("write chart : " + channel)
                channel.push(msg.toString())
            case None => return
        }
    }

    private def toSeq(list: List[Double]): Seq[JsNumber] = {
        list.map(x => JsNumber(x)).toSeq
    }

    def subscribeCharts(username: String, channel: Channel[String], symbol: String) {
        chartSubscribers += username -> ChartClient(symbol, channel)
        //writeHistory(channel, symbol)
    }

    def unSubscribeCharts(username: String) {
        chartSubscribers.remove(username).map {
            client => client.channel.end()
        }
    }


    def onStockUpdate(stock: Stock, priceRhythm: Int) {
        //println(s"[ReaderController] send StockUpdate to ${subscribers.size} subscribers")
        val stockMessage = JsObject(
            Seq("message_type" -> JsNumber(STOCK_UPDATE_MESSAGE), "symbol" -> JsString(stock.symbol),
                "price" -> JsNumber(stock.price), "price_rhythm" -> JsNumber(priceRhythm)))
        val chartMessage = JsObject(
            Seq("message_type" -> JsNumber(CHART_UPDATE_MESSAGE), "symbol" -> JsString(stock.symbol),
                "price" -> JsNumber(stock.price)))

        for (s <- stockSubscribers.values) {
            if (s.symbols.contains(stock.symbol))
                s.channel.push(stockMessage.toString())
        }

        for (s <- chartSubscribers.values) {
            if (s.symbol.equals(stock.symbol))
                s.channel.push(chartMessage.toString())
        }
    }
}
