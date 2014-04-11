package models

import com.novus.salat._
import com.novus.salat.dao._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection

import com.novus.salat.Context

import mongoContext._
import com.jungleo.parameters.MongoConfig._
import com.novus.salat.annotations.raw.Key

/**
 * User: dkovalskyi
 * Date: 20.09.13
 * Time: 12:30
 */
case class Region(@Key("_id") id: Int, title: String, abridgment: String = "", fileName: String = "",
                  description: String = "") {}

object Region extends ModelCompanion[Region, Int] {
    val collection = MongoConnection(MONGO_HOST, MONGO_PORT)(MONGO_DB)("regions")

    def dao: DAO[Region, Int] = new SalatDAO[Region, Int](collection = collection) {}

    val regionOptions: Map[String, Region] = findAll().toList.map(region => region.id.toString -> region).toMap

    val regionIds: Map[Int, Region] = findAll().toList.map(region => region.id -> region).toMap

    val regionPie: List[(Int, String, String, Int)] = getRegionPie

    //lazy val list: List[String] = sectorMap..toList
    def getRegionPie: List[(Int, String, String, Int)] = {
        val grouped = TradingObject.findAll().toList.groupBy(to => to.regionId)
        var list: List[(Int, String, String, Int)] = Nil
        for (region <- grouped)
            list = list ::: List(
                (region._1, regionIds(region._1).title, regionIds(region._1).abridgment, region._2.size))
        list
    }

    def find(regionId: Int): Option[Region] = {
        dao.findOneById(regionId)
    }

    def find(title: String): Option[Region] = {
        dao.findOne(MongoDBObject("title" -> title))
    }
}

