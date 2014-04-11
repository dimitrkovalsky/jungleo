package models

import com.novus.salat.dao._

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.query.Imports._

import mongoContext._
import com.jungleo.parameters.MongoConfig._
import com.novus.salat.annotations.raw.Key

/**
 * User: dkovalskyi
 * Date: 20.09.13
 * Time: 14:03
 */
case class TradingObject(@Key("_id") id: Int, title: String, description: String, regionId: Int, sectorId: Int,
                         var stock: Stock) {
    def apply(stock: Stock): TradingObject = {
        this.stock = stock
        this
    }
}

object TradingObject extends ModelCompanion[TradingObject, Int] {

    private val PAGE_SIZE = 10
    val collection = MongoConnection(MONGO_HOST, MONGO_PORT)(MONGO_DB)("trading_objects")
    val pages = definePages(PAGE_SIZE, findAll().count)

    def dao: DAO[TradingObject, Int] = new SalatDAO[TradingObject, Int](collection = collection) {}

    def find(id: Int): Option[TradingObject] = {
        dao.findOneById(id)
    }

    def findByTitle(title: String): Option[TradingObject] = {
        dao.findOne(MongoDBObject("title" -> title))
    }

    def findStockBySymbol(symbol: String): Option[Stock] = {
        dao.findOne(MongoDBObject("stock.symbol" -> symbol)) match {
            case Some(to: TradingObject) => Some(to.stock)
            case None => None
        }
    }

    def exist(symbol: String): Boolean = {
        findBySymbol(symbol).isDefined
    }

    def findBySymbol(symbol: String): Option[TradingObject] = {
        dao.findOne(MongoDBObject("stock.symbol" -> symbol))
    }

    def findByRegion(regionId: Int): List[TradingObject] = {
        dao.find(MongoDBObject("regionId" -> regionId)).toList
    }

    def findBySector(sectorId: Int): List[TradingObject] = {
        dao.find(MongoDBObject("sectorId" -> sectorId)).toList
    }

    private def definePages(pageSize: Int, totalSize: Int): Int = {
        if (totalSize % pageSize == 0)
            totalSize / pageSize
        else
            totalSize / pageSize + 1
    }

    def getAllTradingObjects(pageNumber: Int): List[TradingObject] = {
        val retrieved = dao.find(MongoDBObject()).skip((pageNumber - 1) * PAGE_SIZE).limit(PAGE_SIZE).toList
        retrieved
    }

    /**
     * Retrieve trading objects and number of pages
     * @param request
     * @param pageNumber
     * @return
     */
    def getTradingObjects(request: Map[String, String], pageNumber: Int): (List[TradingObject], Int) = {
        try {
            val builder = MongoDBObject.newBuilder

            request.get("region").map {
                id => builder += "regionId" -> id.toInt
            }
            request.get("sector").map {
                id => builder += "sectorId" -> id.toInt
            }
            request.get("query").map {
                query => builder += "title" -> (".*" + query + ".*").r
            }
            val newObj = builder.result()
            val cursor = dao.find(newObj)

            val result = cursor.skip((pageNumber - 1) * PAGE_SIZE).limit(PAGE_SIZE).toList
            val size = dao.find(newObj).size
            (result, definePages(PAGE_SIZE, size))
        } catch {
            case t: Throwable => println("Error in TradingObject : " + t.getMessage)
                (Nil, 0)
        }
    }
}