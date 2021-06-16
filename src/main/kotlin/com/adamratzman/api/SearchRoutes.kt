package com.adamratzman.api

import com.adamratzman.sourcing.Country
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.searchRoutes() {
    /**
     * Getting top headlines, by default sorted by insertion date (descending).
     *
     * Sorting is possible by insertion date (ascending or descending) or, when a query is present, by mentions_i.
     *
     * If a query is present, if the *ignore_case* parameter is left null, it is set to true, else as user defines.
     * In the future, let user allow case ignore either globally or per-term with *ignore_case_i*, where i represents the index of the
     * search term.
     *
     * @param source_types potentially empty list of [SourceCategory] ids
     * @param sources sources **to** include
     * @param excluded_sources source to **not** include
     * @param country possibly null [Country] id. If null, results from all countries are displayed
     * @param limit the amount you want to return. -1 if all results are to be returned. Query cost is 20 if all results are to be returned, else 1
     * @param offset the amount to offset. Default is 0.
     * @param categories possibly empty list of categories that snippets should contain
     * @param q possibly empty list of search terms to use. Case sensitivity decided by..
     * @param ignore_case defaults to true. boolean that decides if the search term should ignore case
     * @param sort method of sorting, descending by publish date by default. Allows ascending or, if search terms are present, mentions_i where
     * i is the index of the associated search term
     * @param feeds possibly empty list of **feed ids** separated with __*,__ - format is sourceId*:feedId
     *
     */
    get("/search") {
        val before = System.currentTimeMillis() - 1

        val limit = getLimit(true)
        val offset = getOffset()
        val country = getCountry()

        if (country is ErrorResponse) return@get call.respond(country)
        country as Country?

        val headlines = call.request.queryParameters["headlines"].toBoolean()
        val categories = getCategories()
        val sourceTypes = getSourceTypes()
        val newsSources = getSources()
        val excludedSources = getExcludedSources()
        val feeds = getFeeds()
        var sources = if (headlines) getHeadlineArticles(country) else getSnippets(country)
        if (feeds.isNotEmpty()) {
            sources = sources.filter { source -> feeds.map { it._id }.contains(source.feed._id) }
        }

        // Either news sources can be requested OR a country can. Having oth would make no sense.
        if (newsSources.isNotEmpty()) sources =
            sources.filter { newsSources.map { it.getSnippetId() }.contains(it.feed.source) }
        if (excludedSources.isNotEmpty()) sources =
            sources.filter { !excludedSources.map { it.getSnippetId() }.contains(it.feed.source) }

        if (categories.isNotEmpty()) sources = sources.filter { source ->
            source.snippet.categories != null && source.snippet.categories.map { it.lowercase() }
                .containsAll(categories)
        }
        if (sourceTypes.isNotEmpty()) sources =
            sources.filter { sourceTypes.contains(it.feed.sourceEnum.getCategory()) }

        val searchTerms = getQuery()
        if (searchTerms.isNotEmpty()) sources = sources.filter { container ->
            var candidate = true
            val text = container.snippet.title + " " + container.snippet.description
            searchTerms.forEach { searchTerm ->
                if (!(if (searchTerm.ignoreCase) text.toLowerCase().contains(searchTerm.term.toLowerCase())
                    else text.contains(searchTerm.term))
                ) candidate = false
            }
            candidate
        }

        sources = sources.distinctBy { it.snippet.title }

        val start = call.request.queryParameters["start"]?.toLongOrNull()
        if (start != null) {
            val end = call.request.queryParameters["end"]?.toLongOrNull()
            if (end != null && end < start) {
                return@get call.respond(
                    ErrorResponse(400, "The end time cannot be before the start time!")
                )
            } else sources = sources.filter(start, end)
        }

        if (sources.size < offset) return@get call.respond(
            ErrorResponse(
                400,
                "The specified offset is greater than the total cached headlines"
            )
        )

        val total = if (sources.size < offset + limit) sources.size - offset else limit

        // Sort
        val sortBy = call.request.queryParameters["sort"]
        when (sortBy) {
            null, "descending" -> sources = sources.sortedByDescending { it.snippet.publishDate }
            "ascending" -> sources = sources.sortedBy { it.snippet.publishDate }
            else -> {
                searchTerms.getOrNull(sortBy.removePrefix("mentions_").toIntOrNull() ?: -1)?.let { term ->
                    sources = sources.sortedBy { source ->
                        var mentions = 0
                        val combined = (source.snippet.title + " " + source.snippet.description)
                        val chars = combined.toCharArray()
                        for (i in 0..(chars.size - term.term.length - 1)) {
                            if (combined.substring(i, i + term.term.length)
                                    .equals(term.term, term.ignoreCase)
                            ) mentions++
                        }
                        mentions
                    }
                }
            }
        }

        val items = (if (limit != -1) sources.subList(offset, offset + total) else sources).map { source ->
            source.snippet.toSanitizedSnippet(
                source.feed
            )
        }
        val next = when {
            limit == -1 -> null
            sources.size >= limit * 2 + offset -> "/search" +
                    "?offset=${offset + limit}&limit=$limit" + (if (country != null) "&country=${country.abbrev}" else "")
            else -> null
        }
        val previous = when {
            limit == -1 -> null
            offset - limit >= 0 -> "/search?offset=${offset - limit}" +
                    "&limit=$limit" + (if (country != null) "&country=${country.abbrev}" else "")
            else -> null
        }

        val latency = System.currentTimeMillis() - before

        call.respond(
            Response(
                if (latency < 50) QueryStatus.OKAY else QueryStatus.SLOW,
                latency.toInt(),
                items,
                limit,
                next,
                previous,
                offset,
                sources.size,
                country?.abbrev
            )
        )
    }
}

fun List<SnippetContainer>.filter(start: Long, end: Long?): List<SnippetContainer> {
    return this.filter { it.snippet.publishDate >= start && if (end != null) it.snippet.publishDate <= end else true }
}
