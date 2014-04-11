package models


case class Stock(symbol: String,
                 var price: Double = .0,
                 var volume: Int = 0,
                 var pe: Double = .0,
                 var eps: Double = .0,
                 var week52low: Double = .0,
                 var week52high: Double = .0,
                 var dayLow: Double = .0,
                 var dayHigh: Double = .0,
                 var movInGav50day: Double = .0,
                 var markEtCap: Double = .0
                 ) {

    override def toString: String = {
        "Stock{" + "symbol='" + symbol + '\'' + ", price=" + price + ", volume=" + volume + ", pe=" + pe + ", eps=" + eps + ", week52low=" + week52low + ", week52high=" + week52high + ", dayLow=" + dayLow + ", dayHigh=" + dayHigh + ", movInGav50day=" + movInGav50day + ", markEtCap=" + markEtCap + '}'
    }

    def isChanged(o: Stock): Boolean = {
        if (this eq o)
            return true

        if (dayHigh != o.dayHigh)
            return false

        if (dayLow != dayLow)
            return false

        if (eps != o.eps)
            return false

        if (markEtCap != o.markEtCap)
            return false

        if (movInGav50day != o.movInGav50day) {
            return false
        }
        if (pe != o.pe) {
            return false
        }
        if (price != o.price) {
            return false
        }
        if (volume != o.volume) {
            return false
        }
        if (week52high != o.week52high) {
            return false
        }
        if (week52low != o.week52low) {
            return false
        }
        if (symbol != o.symbol) {
            return false
        }
        true
    }

    def equals(o: Stock): Boolean = {
         o.symbol.equals(o.symbol)
    }

    def isDifferent(stock: Stock): Boolean = {
        !isChanged(stock)
    }
}

/*object Stock {
    def insert(stock: Stock) {
        StockDAO.save(stock)
    }

    def find(stock: Stock) : Option[Stock] =  {
        StockDAO.findOne(MongoDBObject("symbol" -> stock.symbol))
    }

    def update(stock: Stock) {

        find(stock) match {
            case Some(s: Stock) => stock.id = s.id
                StockDAO.update(MongoDBObject("_id" -> s.id), stock, upsert = true, multi = false, new WriteConcern())
                println("DB UPDATE : " + stock)
            case None => insert(stock)
        }
    }
}


object StockDAO extends SalatDAO[Stock, String](collection = MongoConnection("localhost", 27017)("jnmongo")("stocks"))

  */



