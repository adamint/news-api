package com.adamratzman.sourcing

import kotlinx.serialization.Serializable

@Serializable
data class NewsFeed(
    val source: String,
    val sourceEnum: NewsSourceEnum,
    val name: String,
    val url: String,
    val description: String?,
    val language: String,
    val published: Long? = null,
    val _id: String = "${source}_name"
        .lowercase()
        .replace(" ", "_")
        .filter { it.isLetterOrDigit() || it == '_' }
        .let { newName -> if (newName.startsWith("feed_")) newName else "feed_$newName" },
    val headline: Boolean = false,
    val globalHeadline: Boolean
) {

    override fun equals(other: Any?): Boolean {
        return other is NewsFeed && other.source == source && other._id == _id
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + language.hashCode()
        result = 31 * result + (published?.hashCode() ?: 0)
        result = 31 * result + _id.hashCode()
        result = 31 * result + headline.hashCode()
        result = 31 * result + globalHeadline.hashCode()
        return result
    }
}