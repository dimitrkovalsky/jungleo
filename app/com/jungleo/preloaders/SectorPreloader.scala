package com.jungleo.preloaders

import models.{Sector}

/**
 * User: dkovalskyi
 * Date: 20.09.13
 * Time: 13:36
 */
class SectorPreloader extends Preloader {
    def preload(): String = {
        val sectors = List("n/a", "Finance", "Technology", "Health Care", "Public Utilities", "Capital Goods",
            "Consumer Services", "Energy", "Miscellaneous", "Consumer Durables", "Basic Industries", "Transportation",
            "Consumer Non-Durables")
        var id = -1
        Sector.findAll().toList.foreach(Sector.remove)
        val loaded = sectors.map(sector => {
            id += 1
            Sector(id, sector)
        }).map(Sector.save).size

        s"[SectorPreloader] loaded $loaded sectors"
    }
}
