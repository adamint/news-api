package com.adamratzman.api

import com.adamratzman.db.newsFeedCollection
import com.adamratzman.db.snippetCollection
import com.adamratzman.sourcing.NewsFeed
import com.adamratzman.sourcing.NewsSourceEnum
import com.adamratzman.sourcing.Snippet
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById

fun Routing.informationRoutes() {
    route("/information") {
        get("/source-list") {
            val sources = NewsSourceEnum.values().map { it.asData() }

            call.respond(sources)
        }

        get("/source") {
            val before = System.currentTimeMillis()
            val source = call.request.queryParameters["source_id"]?.let { id ->
                NewsSourceEnum.values().firstOrNull { it._id == id }
            }
                ?: return@get call.respond(ErrorResponse(400, "A source was not provided"))

            val feeds = newsFeedCollection.find(NewsFeed::sourceEnum eq source).toList()
            val latency = System.currentTimeMillis() - before

            call.respond(
                Response(
                    if (latency < 50) QueryStatus.OKAY else QueryStatus.SLOW,
                    latency.toInt(),
                    listOf(
                        NewsSourceInformation(
                            source.asData(),
                            feeds.count(),
                            feeds,
                            snippetCollection.find(Snippet::newsSourceEnum eq source).count()
                        )
                    ),
                    -1,
                    null,
                    null,
                    0,
                    1,
                    null
                )
            )
        }
        get("/feed") {
            val before = System.currentTimeMillis()
            val feed = call.request.queryParameters["feed_id"]?.let { newsFeedCollection.findOneById(it) }
                ?: return@get call.respond(ErrorResponse(400, "The feed was not found or not provided"))

            val latency = System.currentTimeMillis() - before
            call.respond(
                Response(
                    if (latency < 50) QueryStatus.OKAY else QueryStatus.SLOW,
                    latency.toInt(),
                    feed,
                    -1,
                    null,
                    null,
                    0,
                    1,
                    null,
                )
            )

        }
        /* get("/information") {
            val before = System.currentTimeMillis()

            val feeds = request.queryParams("feed_ids")?.split("*,")?.mapNotNull { id ->
                val split = id.split("*:")
                if (split.size != 2) null
                else {
                    octagon.newsSources.firstOrNull { it.id == split[0] }?.let { source ->
                        octagon.restApi.newsFeeds.firstOrNull { it.source.toNewsSource(octagon) == source && it.id == split[1] }
                    }
                }
            }

            if (feeds?.isNotEmpty() == true) {
                val feedsInformation = feeds.map {
                    NewsFeedInformation(
                        it,
                        "/api/${user.key}/information?source_id=${it.source.toNewsSource(octagon)}",
                        octagon.restApi.getFeed(it).size
                    )
                }

                val latency = System.currentTimeMillis() - before
                return Response(
                    if (latency < 50) QueryStatus.OKAY else QueryStatus.SLOW,
                    latency.toInt(), feedsInformation, -1, null, null, 0, feedsInformation.size, null,
                    user.addQuery(Query(System.currentTimeMillis(), request.getEndpoint(), latency, 1), octagon)
                )
            }

            call.respond(ErrorResponse(400, "Neither a source nor a news feed was requested"))
        }*/
    }
}