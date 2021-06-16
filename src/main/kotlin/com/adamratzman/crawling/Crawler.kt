package com.adamratzman.crawling

import com.adamratzman.db.feedInfoCollection
import com.adamratzman.db.newsFeedCollection
import com.adamratzman.db.sourceCollection
import com.adamratzman.sourcing.FeedInformation
import com.adamratzman.sourcing.FeedItemType
import com.adamratzman.sourcing.NewsFeed
import com.adamratzman.sourcing.NewsSourceEnum
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import java.io.StringReader
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.absoluteValue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jsoup.Jsoup
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.replaceOneById

open class Crawler(
    val source: NewsSourceEnum,
    val rssBase: String,
    val rssList: String,
    val headlineName: String? = null,
    val globalHeadline: Boolean = false,
    val initialFeedUrls: List<String>? = null
) {
    private val input: SyndFeedInput = SyndFeedInput().apply { isAllowDoctypes = true }
    private val feeds = CopyOnWriteArrayList<NewsFeed>()
    private val httpClient = HttpClient()


    open suspend fun generateFeeds(): List<String> {
        return if (initialFeedUrls != null) initialFeedUrls
        else {
            val rssListPageBody: String = httpClient.get<String>(rssList)
            val links = Jsoup.parse(rssListPageBody).getElementsByTag("a")

            return links.map {
                if (!it.attr("href").startsWith("http")) {
                    getDomain() + it.attr("href")
                } else it.absUrl("href")
            }.distinct()
        }
    }

    suspend fun populateFeeds() = coroutineScope {
        val links = generateFeeds().toMutableList()
        // Handle special cases
        links.removeIf { it.contains("douthat") } // NYT duplication
        println("Populating total ${links.distinct().size} feed LINKS in ${source.readableName}")
        links.distinct().map { url ->
            async {
                try {
                    if (url != rssList && url.contains(rssBase)) {
                        try {
                            println("FEED: ${source.readableName} $url")
                            val feed = getSyndFeed(url, source)
                            if (feed.title != null) {
                                val newsFeed = NewsFeed(
                                    source._id.uppercase(),
                                    source,
                                    feed.title,
                                    url,
                                    feed.description,
                                    feed.language
                                        ?: "en_US",
                                    headline = feed.title == headlineName,
                                    globalHeadline = feed.title == headlineName && globalHeadline
                                )
                                updateFeed(source, newsFeed)
                            }
                        } catch (e: Exception) {
                            /*println("Exception in $url")
                            e.printStackTrace()*/
                        }
                    }
                } catch (e: Exception) {
                    //e.printStackTrace()
                }
            }
        }.awaitAll()
    }

    suspend fun getSyndFeed(url: String, source: NewsSourceEnum, first: Boolean = true): SyndFeed {
        return try {
            val feed = input.build(StringReader(httpClient.get<String>(url)))
            when (source._id) {
                "the_west_au", "usa_today" -> {
                    val title = feed.description
                    feed.description = null
                    feed.title = title
                }
            }
            feed
        } catch (e: Exception) {
            if (!first) throw e
            getSyndFeed(url, source, false)
        }
    }

    private fun getDomain(): String {
        return if (rssList.startsWith("https")) "https://" + rssList.removePrefix("https://").split("/")[0]
        else "http://" + rssList.removePrefix("http://").split("/")[0]
    }

    fun updateFeed(source: NewsSourceEnum, originalFeed: NewsFeed) {
        var newsFeed = originalFeed
        println("Running update check for ${source.readableName} : ${newsFeed.name}.. ${Date().toLocaleString()}")
        try {
            if (sourceCollection.findOneById(source._id) == null) {
                sourceCollection.insertOne(source.asData())
            }

            if (feeds.find { it.url != newsFeed.url && it._id == newsFeed._id } != null) {
                newsFeed = newsFeed.copy(_id = newsFeed._id + "_${newsFeed.url.hashCode().absoluteValue}")
            } else feeds.add(newsFeed)

            val newFeedInfo = FeedInformation(
                source,
                newsFeed._id,
                newsFeed.url,
                newsFeed.published,
                headline = newsFeed.headline,
                globalHeadline = globalHeadline
            )

            if (feedInfoCollection.findOne(FeedInformation::url eq newsFeed.url) == null) {
                feedInfoCollection.insertOne(newFeedInfo)
                println("Inserted feed info for ${newsFeed.name} (${source.readableName})")
            } else feedInfoCollection.replaceOneById(newsFeed._id, newFeedInfo)

            if (newsFeedCollection.findOne(NewsFeed::_id eq newsFeed._id) == null) {
                newsFeedCollection.insertOne(newsFeed)
                println("Inserted news feed for ${newsFeed.name} (${source.readableName})")
            } else newsFeedCollection.replaceOneById(newsFeed._id, newsFeed)

            println(
                "Done with update check for ${source.readableName} : ${newsFeed.name}.. ${
                    Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime().toString()
                }"
            )
        } catch (e: Exception) {
            //e.printStackTrace()
        }
    }
}
