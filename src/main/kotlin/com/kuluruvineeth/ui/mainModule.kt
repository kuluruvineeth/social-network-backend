package com.kuluruvineeth.ui

import com.kuluruvineeth.controller.user.UserController
import com.kuluruvineeth.controller.user.UserControllerImpl
import com.kuluruvineeth.util.Constants
import org.koin.dsl.module
import org.litote.kmongo.coroutine.CoroutineClient
import com.mongodb.reactivestreams.client.MongoClient
import org.litote.kmongo.reactivestreams.*  // KMongo reactivestreams extensions
import org.litote.kmongo.coroutine.*

private val MongoClient.coroutine: CoroutineClient get() = CoroutineClient(this)



val mainModule = module {
    single {
        val client = KMongo.createClient().coroutine
        client.getDatabase(Constants.DATABASE_NAME)
    }
    single<UserController>{
        UserControllerImpl(get())
    }
}