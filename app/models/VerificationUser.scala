package models

import com.novus.salat.dao._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection


import mongoContext._
import com.jungleo.parameters.MongoConfig._
import com.novus.salat.annotations.raw.Key

/**
 * User: dkovalskyi
 * Date: 27.09.13
 * Time: 16:18
 */
case class VerificationUser(@Key("_id") id: String, username: String) {
}

object VerificationUser extends ModelCompanion[VerificationUser, String] {
    private val collection = MongoConnection(MONGO_HOST, MONGO_PORT)(MONGO_DB)("verification_user")

    def dao: DAO[VerificationUser, String] = new SalatDAO[VerificationUser, String](collection = collection) {}

    def exists(id: String): Boolean = {
        find(id).isDefined
    }

    def find(id: String): Option[VerificationUser] = {
        try {
            dao.findOne(MongoDBObject("_id" -> id))
        } catch {
            case t: Throwable => println(t.getMessage); None
        }
    }


    def remove(id: String, name:String) {
        dao.remove(VerificationUser(id, name))
    }


}
