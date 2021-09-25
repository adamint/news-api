package com.adamratzman.api

import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.serialization.json.Json
import org.bson.conversions.Bson
import org.litote.kmongo.and

class NewsHttpApi {
    init {
        embeddedServer(CIO, port = 8000) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                })
            }

            install(CORS) {
                anyHost()
                method(HttpMethod.Options)
                method(HttpMethod.Get)
            }


            routing {
                informationRoutes()
                statisticsRoutes()
                searchRoutes()
            }
        }.start(wait = true)

    }
}

fun buildRequest(vararg filters: Bson?) = and(filters.filterNotNull())