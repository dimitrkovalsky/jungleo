package models

import com.novus.salat._
import com.novus.salat.dao._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection

import com.novus.salat.Context

import mongoContext._
import com.novus.salat.annotations.raw.Key

case class User(username: String, fullName:String, password: String, email: String, profile: UserProfile, public: Boolean, var confirmed: Boolean = false, @Key("_id") var id: ObjectId = new ObjectId) {}

case class UserProfile(country: Option[String], gender: Option[String], age: Option[Int], professional: Option[String] = None)

case class UserViewProfile(id: String, username: String, profile: UserProfile)

case class UserViewFullProfile(id: String, username: String, verified:Boolean, profile: UserProfile, portfolio: Portfolio)


object User {
    def insert(user: User): Option[ObjectId] = {
        println("Saved to database : " + user.username)
        UserDAO.insert(user) match {
            case Some(id: ObjectId) => Some(id)
            case None => None
        }
    }

    // TODO: encrypt password
    def authenticate(username: String, password: String): Option[User] = {
        try {
        //    println("[User] Authenticate : " + username + " \t " + password)
      //      println("Search result" + User.find(username)     )
            UserDAO.findOne(MongoDBObject("username" -> username, "password" -> password)) match {
                case Some(user: User) => Some(user)
                case None => None
            }
        } catch {
            case _: Throwable => None
        }
    }

    def exists(username: String): Boolean = {
        find(username).isDefined
    }

    def find(username: String): Option[User] = {
        try {
            UserDAO.findOne(MongoDBObject("username" -> username))
        } catch {
            case t: Throwable => println(t); None
        }
    }

    def findById(userId: String): Option[User] = {
        try {
            UserDAO.findOne(MongoDBObject("_id" -> new ObjectId(userId)))
        } catch {
            case _: Throwable => None
        }
    }

    def findLike(query: String): List[UserViewProfile] = {
        try {
            print("User find like : " + query)
            UserDAO.find(MongoDBObject("username" -> s"$query")).toList.map(u => UserViewProfile(u.id.toString, u.username, u.profile))
        } catch {
            case _: Throwable => Nil
        }
    }
}

object UserDAO extends SalatDAO[User, ObjectId](collection = MongoConnection("localhost", 27017)("jnmongo")("users"))

