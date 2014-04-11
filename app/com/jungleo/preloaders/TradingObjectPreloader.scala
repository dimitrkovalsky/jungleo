package com.jungleo.preloaders

import models._
import scala.io.Source
import models.Stock


/**
 * User: dkovalskyi
 * Date: 20.09.13
 * Time: 14:44
 */
class TradingObjectPreloader extends Preloader {

    val regions = Region.findAll().toList

    def preload(): String = {
        TradingObject.findAll().toList.foreach(TradingObject.remove)
        StockChart.findAll().toList.foreach(StockChart.remove)
        JungleoSequence.resetTradingObjectSequence()
        println("[TradingObjectPreloader] trading objects cleaned")

        val loader = new SymbolLoader
        var toId = 0
        var loaded = 0
        var history: List[Double] = Nil
        for (i <- 1 to 50)
            history = history ::: List(0d)
        for (region <- regions) {
            val loadedFromFile = loader.readFile(region.fileName, region.id)
            for (stockLine <- loadedFromFile) {
                toId = JungleoSequence.nextTradingObjectId()
                val to = TradingObject(toId, stockLine.name, "", stockLine.regionId, stockLine.sectorId,
                    Stock(stockLine.symbol))
                TradingObject.save(to)
                val chart = StockChart(to.stock.symbol, history)
                StockChart.save(chart)
            }
            loaded += loadedFromFile.size
        }
        s"[TradingObjectPreloader] loaded $loaded trading objects"
    }
}

class SymbolLoader {
    private val NA = "n/a"
    val sectorMap = Sector.sectorMap
    var sectorsWithoutNA: Map[String, Sector] = Sector.findAll().toList.filterNot(s => s.id == 0)
        .map(sec => sec.title -> sec).toMap

    def readFile(fileName: String, regionId: Int): List[StockLine] = {
        // println(sectors)
        var i = 0
        var list: List[StockLine] = Nil
        for (line <- Source.fromFile(s"data/$fileName.csv").getLines()) {
            i += 1
            if (i > 0) {
                val stockLine = parse(line, regionId)
                //     println(s"Get : ${stockLine.sector} \t ${getSectorId(stockLine.sector)}")
                getSectorId(stockLine.sector) map {
                    id => stockLine.sectorId = id
                        list = list ::: List(stockLine)
                }
            }
        }
        list
    }

    def getSectorId(sectorName: String): Option[Int] = {
        for (key <- sectorMap.keys) {
            if (sectorName.toLowerCase.contains(key.toLowerCase))
                return Some(sectorMap(key).id)
        }
        None
    }

    def parse(line: String, regionId: Int): StockLine = {
        val array = line.split(",").toList.map(s => s.substring(1, s.size - 1))

        val stock = StockLine(array(0), array(1), array(5), array(7), array(8), regionId)

        getRealSector(array).map {
            sector => stock.sector = sector
        }
        stock
    }

    def getRealSector(array: List[String]): Option[String] = {
        for (str <- array)
            if (sectorsWithoutNA.get(str).isDefined)
                return Some(str)
        None
    }

}

case class StockLine(symbol: String, name: String, country: String, var sector: String, industry: String, regionId: Int,
                     var sectorId: Int = 0)

