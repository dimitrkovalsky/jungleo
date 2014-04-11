package models

import com.novus.salat.dao._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection


import mongoContext._
import com.jungleo.parameters.MongoConfig._
import com.novus.salat.annotations.raw.Key

/**
 * User: dkovalskyi
 * Date: 20.09.13
 * Time: 13:37
 */
case class Sector(@Key("_id") id: Int, title: String, description: String = "") {}

object Sector extends ModelCompanion[Sector, Int] {
    private val collection = MongoConnection(MONGO_HOST, MONGO_PORT)(MONGO_DB)("sectors")

    val sectorOptions: Map[String, Sector] = findAll().toList.map(sector => sector.id.toString -> sector).toMap
    val sectorIds: Map[Int, Sector] = findAll().toList.map(sector => sector.id -> sector).toMap
    val sectorMap: Map[String, Sector] = findAll().toList.map(sector => sector.title -> sector).toMap
    val sectorPie: List[(Int, String, Int)] = getSectorPie

    def getSectorPie: List[(Int, String, Int)] = {
        val grouped = TradingObject.findAll().toList.groupBy(to => to.sectorId)
        var list: List[(Int, String, Int)] = Nil
        for (sector <- grouped)
            list = list ::: List((sector._1, sectorIds(sector._1).title, sector._2.size))
        list
    }

    def dao: DAO[Sector, Int] = new SalatDAO[Sector, Int](collection = collection) {}

    def find(id: Int): Option[Sector] = {
        dao.findOneById(id)
    }

    def find(title: String): Option[Sector] = {
        dao.findOne(MongoDBObject("title" -> title))
    }
}
