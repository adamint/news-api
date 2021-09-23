package com.adamratzman.api

import com.adamratzman.sourcing.Country
import com.adamratzman.sourcing.NewsFeed
import com.adamratzman.sourcing.NewsSourceData
import com.adamratzman.sourcing.Snippet
import kotlinx.serialization.Serializable

@Serializable
data class Response<out T>(val status: QueryStatus, val latency_ms: Int, val items: T, val limit: Int, val next: String? = null,
                           val previous: String? = null, val offset: Int = 0, val total: Int, val country: String? = null)

enum class QueryStatus {
    GOOD, OKAY, UNAVAILABLE, SLOW, NA;

    override fun toString() = name.lowercase()

    companion object {
        fun fromLatency(latencyInMs: Long): QueryStatus {
            return if (latencyInMs == -1L) NA
            else if (latencyInMs < 400) GOOD
            else if (latencyInMs < 800) OKAY
            else SLOW
        }
    }
}

@Serializable
data class ErrorResponse(val code: Int, val message: String)

@Serializable
data class Statistics(
    val total_sources: Int,
    val sources_per_country: Map<Country, Int>,
    val total_feeds: Int,
    val total_articles_cached: Int,
    val total_insertion_rate: Float,
    val day_insertion_rate: Float,
)

@Serializable
data class SearchTerm(val term: String, val ignoreCase: Boolean)

@Serializable
data class SnippetContainer(val snippet: Snippet, val feed: NewsFeed)

@Serializable
data class NewsSourceInformation(val source: NewsSourceData, val feed_size: Int, val feeds: List<NewsFeed>, val article_count: Int)

@Serializable
data class NewsFeedInformation(val feed: NewsFeed, val source_url: String, val article_count: Int)

@Serializable
data class SanitizedSnippet(val title: String, val author: String?, val published_at: Long, val description: String?,
                            val link: String, val source: NewsSourceData, val feed_id: String)

fun Snippet.toSanitizedSnippet(feed: NewsFeed): SanitizedSnippet {
    return  SanitizedSnippet(title,
        if (author?.isEmpty() == true) null else author?.lowercase()?.split(" ")?.joinToString(" ") { if (it == "and") it else it.capitalize() },
        publishDate,
        if (description?.isEmpty() == true) null else description,
        url,
        newsSourceEnum.asData(),
        "${this.newsSourceEnum._id}*:${feed._id}"
    )
}
