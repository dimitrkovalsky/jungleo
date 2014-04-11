package com.jungleo.preloaders

/**
 * User: dkovalskyi
 * Date: 20.09.13
 * Time: 13:02
 */
object PreloadRunner {
    def load(active: Boolean = false) {
        if (!active)
            return
        val preloaders: List[Preloader] = new RegionPreloader :: new SectorPreloader :: Nil
        preloaders.foreach(p => println(p.preload()))
        println(new TradingObjectPreloader().preload())
    }
}
