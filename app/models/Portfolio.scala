package models

import com.novus.salat._
import com.novus.salat.dao._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection

import com.novus.salat.Context
import com.jungleo.parameters.MongoConfig._
import mongoContext._
import com.novus.salat.annotations.raw.Key

/**
 * User: Dimitr
 * Date: 19.09.13
 * Time: 12:18
 */
case class Portfolio(username: String, @Key("_id") id: ObjectId = new ObjectId, var assets: List[Asset] = Nil) {
    def hasAsset(symbol: String): Boolean = {
        assets.exists(asset => asset.symbol.equals(symbol))
    }


    def buy(asset: Asset) = {
        assets = assets ::: List(asset)
        println("[Portfolio] buy : " + asset)
    }

    def sell(asset: Asset) = {
        assets = assets.filterNot(as => as.symbol.equals(asset.symbol))
        println("[Portfolio] sell : " + asset)
    }
}

object Portfolio extends ModelCompanion[Portfolio, Int] {


    val collection = MongoConnection(MONGO_HOST, MONGO_PORT)(MONGO_DB)("portfolios")

    def dao: DAO[Portfolio, Int] = new SalatDAO[Portfolio, Int](collection = collection) {}


    def findByUsername(username: String): Option[Portfolio] = {
        dao.findOne(MongoDBObject("username" -> username))
    }

    def findByUserId(userId: String): Option[Portfolio] = {
        dao.findOne(MongoDBObject("username" -> new ObjectId(userId)))
    }
}

case class Asset(symbol: String, amount: Int = 0)
