package models

import com.novus.salat.dao._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection


import mongoContext._
import com.jungleo.parameters.MongoConfig._
import com.novus.salat.annotations.raw.Key

/**
 * User: dkovalskyi
 * Date: 26.09.13
 * Time: 18:02
 */
case class JungleoSequence(@Key("_id") id: Int, value: Int) {

}


object JungleoSequence extends ModelCompanion[JungleoSequence, Int] {
    val TRADING_OBJECT_SEQUENCE = 1

    private val collection = MongoConnection(MONGO_HOST, MONGO_PORT)(MONGO_DB)("sequences")

    def dao: DAO[JungleoSequence, Int] = new SalatDAO[JungleoSequence, Int](collection = collection) {}

    def resetTradingObjectSequence() {
        dao.save(JungleoSequence(TRADING_OBJECT_SEQUENCE, 1))
    }

    def nextTradingObjectId(): Int = {
        val sequenceNext = dao.findOne(MongoDBObject("_id" -> TRADING_OBJECT_SEQUENCE)).get.value + 1
        dao.save(JungleoSequence(TRADING_OBJECT_SEQUENCE, sequenceNext))
        sequenceNext
    }
}
