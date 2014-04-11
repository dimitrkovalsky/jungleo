package com.jungleo.preloaders

import controllers.Admin
import models._
import com.mongodb.casbah.commons.Imports._
import models.UserProfile

import com.novus.salat._
import com.novus.salat.dao._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection

import com.novus.salat.Context
import com.jungleo.parameters.MongoConfig._
import mongoContext._
import com.novus.salat.annotations.raw.Key

/**
 * User: dkovalskyi
 * Date: 26.09.13
 * Time: 17:33
 */
class UserPreloader extends Preloader {
    def preload(): String = {
        val user = User(Admin.ADMIN_USERNAME, "Super admin", Admin.ADMIN_PASSWORD, "admin@mail.com", UserProfile(None, None, None), true)
        if (!User.find(Admin.ADMIN_USERNAME).isDefined) {
            val id: ObjectId = User.insert(user).get
            Portfolio.insert(Portfolio(user.username, id))
        }
        ""

    }
}
