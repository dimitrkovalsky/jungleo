package com.jungleo.preloaders

import models.Region

/**
 * User: dkovalskyi
 * Date: 20.09.13
 * Time: 12:30
 */
class RegionPreloader extends Preloader {
    override def preload(): String = {
        val regions = List(Region(1, "North America", "NA", "North America"),
            Region(2, "South America", "SA", "South America"), Region(3, "Asia", "AS", "Asia"),
            Region(4, "Australia & Pacific", "AU", "Australia and South Pacific"),
            Region(5, "Central America", "CA", "Central America and Caribbean"), Region(6, "Europe", "EU", "Europe"),
            Region(7, "Middle East", "ME", "Middle East"))
        Region.findAll().toList.foreach(Region.remove)
        regions.foreach(Region.save)

        s"[RegionPreloader] loaded ${regions.size} regions"
    }
}
