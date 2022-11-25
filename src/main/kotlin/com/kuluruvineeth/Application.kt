package com.kuluruvineeth

import io.ktor.server.application.*
import com.kuluruvineeth.plugins.*
import com.kuluruvineeth.ui.mainModule
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    install(Koin){
        modules(mainModule)
    }
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureRouting()
}
