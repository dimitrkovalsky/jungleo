package com.jungleo.fetcher

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection
import java.util.logging.Level
import java.util.logging.Logger
import models.{TradingObject, Stock}

object StockFetcher {
    def getStock(symbol: String): Stock = {
        val sym: String = symbol.toUpperCase
        var price: Double = 0.0
        var volume: Int = 0
        var pe: Double = 0.0
        var eps: Double = 0.0
        var week52low: Double = 0.0
        var week52high: Double = 0.0
        var dayLow: Double = 0.0
        var dayHigh: Double = 0.0
        var movInGav50day: Double = 0.0
        var markEtCap: Double = 0.0
        try {
            val yahoo: URL = new URL("http://finance.yahoo.com/d/quotes.csv?s=" + symbol + "&f=l1vr2ejkghm3j3")
            val connection: URLConnection = yahoo.openConnection
            val is: InputStreamReader = new InputStreamReader(connection.getInputStream)
            val br: BufferedReader = new BufferedReader(is)
            val line: String = br.readLine
            val stockInfo: Array[String] = line.split(",")
            val sh: StockHelper = new StockHelper
            price = sh.handleDouble(stockInfo(0))
            volume = sh.handleInt(stockInfo(1))
            pe = sh.handleDouble(stockInfo(2))
            eps = sh.handleDouble(stockInfo(3))
            week52low = sh.handleDouble(stockInfo(4))
            week52high = sh.handleDouble(stockInfo(5))
            dayLow = sh.handleDouble(stockInfo(6))
            dayHigh = sh.handleDouble(stockInfo(7))
            movInGav50day = sh.handleDouble(stockInfo(8))
            markEtCap = sh.handleDouble(stockInfo(9))
        } catch {
            case e: IOException => {
                val log: Logger = Logger.getLogger("StockFetcher")
               // log.log(Level.SEVERE, "[StockFetcher] " + e.toString, e)
                return null
            }
        }
        Stock(sym, price, volume, pe, eps, week52low, week52high, dayLow, dayHigh, movInGav50day, markEtCap)
    }

    def initTradingObjects(): scala.collection.mutable.Map[String, TradingObject] = {
        val map = scala.collection.mutable.Map[String, TradingObject]()
        TradingObject.findAll().toList.map(to => map += to.stock.symbol -> to)
        map
    }
}


