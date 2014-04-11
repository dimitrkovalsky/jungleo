package models

import com.novus.salat.dao._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection


import mongoContext._
import com.jungleo.parameters.MongoConfig._
import com.novus.salat.annotations.raw.Key

/**
 * User: Dimitr
 * Date: 24.09.13
 * Time: 9:44
 */
case class StockChart(@Key("_id") id: String, var history: List[Double] = Nil) {
    private final val HISTORY_SIZE = 50

    def addPrice(price: Double) : StockChart = {
        history = history ::: List(price)
        if (history.size > HISTORY_SIZE)
            history = history.tail
        this
    }
}

object StockChart extends ModelCompanion[StockChart, String] {
    private val collection = MongoConnection(MONGO_HOST, MONGO_PORT)(MONGO_DB)("stock_charts")

    def dao: DAO[StockChart, String] = new SalatDAO[StockChart, String](collection = collection) {}

    def exists(symbol: String): Boolean = {
        findBySymbol(symbol).isDefined
    }

    def findBySymbol(symbol: String): Option[StockChart] = {
        try {
            dao.findOne(MongoDBObject("_id" -> symbol))
        } catch {
            case t: Throwable => println(t.getMessage); None
        }
    }
}
