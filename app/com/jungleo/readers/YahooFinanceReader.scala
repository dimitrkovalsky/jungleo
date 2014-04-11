package com.jungleo.readers

import java.util.logging.Logger
import models.{StockChart, TradingObject, Stock}
import com.jungleo.fetcher.StockFetcher
import controllers.ReaderController

/**
 * User: dkovalskyi
 * Date: 16.09.13
 * Time: 18:07
 */
object PriceRhythm {
    val UP = 1
    val DOWN = 2
    val SAME = 3
}

class YahooFinanceReader(tradingObjects: scala.collection.mutable.Map[String, TradingObject], random: Boolean) extends Runnable {

    private val logger: Logger = Logger.getLogger("YahooFinanceReader")


    private def poll() {
        for (symbol <- tradingObjects.keys) {
            val stock = StockFetcher.getStock(symbol)
            if (stock != null && stock.isDifferent(tradingObjects(symbol).stock)) {
                 println("UPDATE Stock : " + stock)
                val previousPrice = tradingObjects(symbol).stock.price
                val rhythm = if (previousPrice < stock.price)
                    PriceRhythm.UP
                else if (previousPrice > stock.price)
                    PriceRhythm.DOWN
                else
                    PriceRhythm.SAME
                ReaderController.onStockUpdate(stock, rhythm)

                tradingObjects(symbol).stock = stock
                TradingObject.save(tradingObjects(symbol))
                StockChart.findBySymbol(symbol).map {
                    chart => StockChart.save(chart.addPrice(stock.price))
                }
            }
        }

    }

    def run() {
        if (random)
            pollRandom()
        else
            poll()
    }

    private def pollRandom() {
        for (symbol <- tradingObjects.keys) {
            val previousPrice = tradingObjects(symbol).stock.price
            val stock = getRandomStock(tradingObjects(symbol).stock)
            val rhythm = if (previousPrice < stock.price)
                PriceRhythm.UP
            else if (previousPrice > stock.price)
                PriceRhythm.DOWN
            else
                PriceRhythm.SAME

            //println("RANDOM UPDATE Stock : " + stock)
            ReaderController.onStockUpdate(stock, rhythm)
            tradingObjects(symbol).stock = stock
            TradingObject.save(tradingObjects(symbol))
        }
    }

    private def getRandomStock(stock: Stock): Stock = {
        var random = Math.random() * 10 - 5
        if (stock.price + random <= 0)
            random = Math.abs(random)
        var fraction = 0.1 * ((stock.price + random) / 10).ceil
        fraction = if (Math.random() >= 0.5) fraction else fraction * -1
        stock.price = (stock.price + random).round + fraction
        if (stock.price > 10000)
            stock.price = 1
        stock
    }
}

