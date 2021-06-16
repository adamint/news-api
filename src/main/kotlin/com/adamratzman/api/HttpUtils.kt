package com.adamratzman.api

import com.adamratzman.db.newsFeedCollection
import com.adamratzman.db.snippetCollection
import com.adamratzman.sourcing.Country
import com.adamratzman.sourcing.NewsFeed
import com.adamratzman.sourcing.NewsSourceEnum
import com.adamratzman.sourcing.Snippet
import com.adamratzman.sourcing.SourceCategory
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.litote.kmongo.and
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOne

fun PipelineContext<Unit, ApplicationCall>.getLimit(allowInfinite: Boolean): Int {
    val limit = call.request.queryParameters["limit"]?.toIntOrNull()
    return when {
        limit == null -> 20
        limit in 1..50 -> limit
        limit <= 0 && allowInfinite -> -1
        else -> 20
    }
}

fun PipelineContext<Unit, ApplicationCall>.getOffset(): Int {
    val offset = call.request.queryParameters["offset"]?.toIntOrNull()
    return if (offset == null) 0 else if (offset < 0) 0 else offset
}

fun PipelineContext<Unit, ApplicationCall>.getCountry(): Any? {
    val country = call.request.queryParameters["country"]
    return if (country == null) null else Country.values().firstOrNull { it.abbrev == country }
        ?: ErrorResponse(400, "A country with this id was not found")
}

fun PipelineContext<Unit, ApplicationCall>.getSourceTypes(): List<SourceCategory> {
    return call.request.queryParameters["source_types"]?.split(",")
        ?.mapNotNull { id -> SourceCategory.values().firstOrNull { it.id == id } } ?: listOf()
}

fun PipelineContext<Unit, ApplicationCall>.getCategories(): List<String> {
    return call.request.queryParameters["categories"]?.split(",")?.map { it.lowercase() } ?: listOf()
}

fun PipelineContext<Unit, ApplicationCall>.getSources(): List<NewsSourceEnum> {
    return call.request.queryParameters["sources"]?.split(",")
        ?.mapNotNull { id -> NewsSourceEnum.values().firstOrNull { it._id == id } } ?: listOf()
}

fun PipelineContext<Unit, ApplicationCall>.getExcludedSources(): List<NewsSourceEnum> {
    return call.request.queryParameters["excluded_sources"]?.split(",")
        ?.mapNotNull { id -> NewsSourceEnum.values().firstOrNull { it._id == id } } ?: listOf()
}

fun PipelineContext<Unit, ApplicationCall>.getQuery(): List<SearchTerm> {
    return call.request.queryParameters["q"]?.split(",")
        ?.distinct()?.map { SearchTerm(it, call.request.queryParameters["ignore_case"]?.toBoolean() ?: true) }
        ?: listOf()
}

fun PipelineContext<Unit, ApplicationCall>.getAuthor(): List<String> {
    return call.request.queryParameters["authors"]?.split(",")?.map { it.lowercase() }
        ?: listOf()
}

fun PipelineContext<Unit, ApplicationCall>.getFeeds(): List<NewsFeed> {
    return call.request.queryParameters["feeds"]?.split("*,")
        ?.mapNotNull {
            val feedIdSplit = it.split("*:")
            println(NewsSourceEnum.values().firstOrNull { it._id == feedIdSplit.getOrNull(0) })
            NewsSourceEnum.values().firstOrNull { it._id == feedIdSplit.getOrNull(0) }?.let { source ->
                newsFeedCollection.findOne(
                    and(
                        NewsFeed::_id eq feedIdSplit.getOrNull(1),
                        NewsFeed::sourceEnum eq source
                    )
                )
            }.apply { println(this) }
        } ?: listOf()
}

fun getHeadlineFeeds(country: Country?): List<NewsFeed> {
    return if (country == null) newsFeedCollection.find(NewsFeed::globalHeadline eq true).toList()
    else {
        newsFeedCollection.find(
            and(
                NewsFeed::headline eq true,
                NewsFeed::sourceEnum / NewsSourceEnum::country eq country
            )
        ).toList()
    }
}

suspend fun getHeadlineArticles(country: Country?): List<SnippetContainer> = coroutineScope{
    val sourceSnippets = getHeadlineFeeds(country)
        .map { newsFeed ->
            async { snippetCollection.find(Snippet::newsFeed / NewsFeed::_id eq newsFeed._id).toList() }
        }.awaitAll()

    val intermixedHeadlines = mutableListOf<SnippetContainer>()
    for (i in 0..(sourceSnippets.map { it.size }.maxOrNull() ?: -1)) {
        sourceSnippets.forEach { if (it.size > i) intermixedHeadlines.add(it[i].toSnippetContainer()) }
    }

    intermixedHeadlines
}

fun getSnippets(country: Country?): List<SnippetContainer> {
    val snippets =
        if (country != null) snippetCollection.find(Snippet::newsFeed / NewsFeed::sourceEnum / NewsSourceEnum::country eq country)
            .toList()
        else snippetCollection.find().toList()
    return snippets.map { it.toSnippetContainer() }
}

fun Snippet.toSnippetContainer() = SnippetContainer(this, newsFeed)