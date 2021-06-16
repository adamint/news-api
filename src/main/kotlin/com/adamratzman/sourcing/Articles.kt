package com.adamratzman.sourcing

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Snippet(
    val title: String,
    val author: String? = null,
    val publishDate: Long,
    val description: String? = null,
    @SerialName("_id") val url: String,
    val newsFeed: NewsFeed,
    val newsSourceEnum: NewsSourceEnum,
    val source: String,
    val categories: List<String>? = null,
    val insertionDate: Long = System.currentTimeMillis()
)

enum class FeedItemType {
    Article,
    FeedInformation,
    NewsFeed
}

@Serializable
data class Article(
    val snippet: Snippet,
    val text: String,
    val lastEditedAt: Long? = null,
    @Contextual val _id: Id<Article> = newId()
)

@Serializable
data class FeedInformation(
    val source: NewsSourceEnum,
    val feedId: String,
    val url: String,
    val published: Long? = null,
    @Contextual val _id: Id<FeedInformation> = newId(),
    val headline: Boolean = false,
    val globalHeadline: Boolean,
)

enum class Subject(val readable: String) {
    POLITICS("politics"),
    BUSINESS("business"),
    ENTERTAINMENT("entertainment"),
    SPORTS("sports"),
    COMIC("comic"),
    OTHER("other"),
    REAL_ESTATE("real estate"),
    LIFESTYLE("lifestyle"),
    OPINIONS("opinions"),
    LOCAL("local"),
    GENERAL_NEWS("general news"),
    FAITH("faith"),
    WEATHER("weather")
    ;

    override fun toString() = readable
}

enum class NewsScope(val readable: String) {
    WORLD("world"),
    NATIONAL("national"),
    LOCAL("local"),
    OTHER("other")
    ;

    override fun toString() = readable
}
