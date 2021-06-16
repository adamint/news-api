package com.adamratzman.crawling

import com.adamratzman.db.feedInfoCollection
import com.adamratzman.db.newsFeedCollection
import com.adamratzman.db.snippetCollection
import com.adamratzman.db.sourceCollection
import com.adamratzman.sourcing.FeedInformation
import com.adamratzman.sourcing.NewsFeed
import com.adamratzman.sourcing.NewsSourceEnum
import com.adamratzman.sourcing.Snippet
import com.adamratzman.utils.clean
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import java.io.StringReader
import java.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jsoup.Jsoup
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.replaceOneById

val input: SyndFeedInput = SyndFeedInput()

suspend fun updateSnippets() {
    coroutineScope {
        p("Source Retrieval", "Retrieving source list from database")
        val newsSources = sourceCollection.find().toList()
        NewsSourceEnum.values().filter { enum -> newsSources.any { it._id == enum._id } }.map { newsSource ->
            async {
                p("Source Retrieval", "Generating feeds for ${newsSource.readableName}")
                val feedInformations = feedInfoCollection.find(FeedInformation::source eq newsSource).toList()
                feedInformations.map { feedInformation ->
                    async { updateFeedInfo(newsSource, feedInformation) }
                }.awaitAll()
            }
        }.awaitAll()
    }
}

fun updateFeedInfo(newsSource: NewsSourceEnum, feedInformation: FeedInformation) {
    p("Feed Update IP", "Starting update for ${feedInformation.feedId} (${newsSource.readableName})")
    val before = System.currentTimeMillis()
    val syndFeed = getSyndFeed(feedInformation.url, newsSource)
    p(
        "Feed Update IP",
        "Retrieved feed for ${feedInformation.feedId} (${newsSource.readableName}) in ${System.currentTimeMillis() - before} ms"
    )
    val feed = NewsFeed(
        newsSource._id.uppercase(),
        newsSource,
        syndFeed.title,
        feedInformation.url,
        syndFeed.description,
        syndFeed.language
            ?: "en_US",
        syndFeed.publishedDate?.toInstant()?.toEpochMilli()
            ?: System.currentTimeMillis(),
        headline = feedInformation.headline,
        globalHeadline = feedInformation.globalHeadline
    )

    var count = 0
    getSnippets(feed, syndFeed, newsSource).forEach { snippet ->
        val snippetDoesntExist = snippetCollection.findOneById(snippet.url) == null
        if (snippetDoesntExist) {
            snippetCollection.insertOne(snippet)
            count++
        }
    }

    newsFeedCollection.replaceOneById(feed._id, feed)

    p(
        "Feed Update Done",
        if (count == 0) "Done | No change in ${feed.name}" else "Added $count snippets in ${feed.name} - ${feed.source} - ${Date().toLocaleString()}"
    )
}

fun getSnippets(newsFeed: NewsFeed, feed: SyndFeed, source: NewsSourceEnum): List<Snippet> {
    val newFeedSnippet = feed.entries.mapNotNull { entry ->
        if (entry.link != null || entry.uri != null) {
            Snippet(
                entry.title.clean(),
                entry.author.clean(),
                entry.publishedDate?.time
                    ?: System.currentTimeMillis(),
                entry.description?.value?.let { Jsoup.parse(it).text().clean() },
                entry.link?.clean()
                    ?: entry.uri.clean(),
                newsFeed,
                source,
                source._id.uppercase(),
                entry.categories?.map { it.name.clean() },
                insertionDate = entry.publishedDate?.time ?: System.currentTimeMillis()
            )
        } else null
    }.distinct().toMutableList()
    return newFeedSnippet
}


fun getSyndFeed(url: String, source: NewsSourceEnum, first: Boolean = true): SyndFeed {
    return try {
        val feed = input.build(StringReader(Jsoup.connect(url).userAgent("Octagon | Open Source API").get().html()))
        when (source._id) {
            "the_west_au", "usa_today" -> {
                val title = feed.description
                feed.description = null
                feed.title = title
            }
        }
        feed
    } catch (e: Exception) {
        if (!first) throw Exception("Unable to parse $url - (${source.readableName})", e)
        getSyndFeed(url, source, false)
    }
}

fun p(category: String, obj: Any) = println("$category | $obj")
//fun get(url: String)