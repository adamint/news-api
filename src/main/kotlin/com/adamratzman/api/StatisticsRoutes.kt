package com.adamratzman.api

import com.adamratzman.db.newsFeedCollection
import com.adamratzman.db.snippetCollection
import com.adamratzman.db.sourceCollection
import com.adamratzman.sourcing.Snippet
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import org.litote.kmongo.and
import org.litote.kmongo.ascendingSort
import org.litote.kmongo.gte
import org.litote.kmongo.lte

fun Routing.statisticsRoutes() {
    get("/statistics") {
        val before = System.currentTimeMillis()
        val sources = sourceCollection.find().toList()
        val statistics = Statistics(
            sources.size,
            sources.groupBy { it.country }.map { (country, entries) -> country to entries.size }.toMap(),
            newsFeedCollection.countDocuments().toInt(),
            snippetCollection.countDocuments().toInt(),
            getInsertionRate(),
            getInsertionRate(start = System.currentTimeMillis() - (1000 * 60 * 60 * 24))
        )
        val latency = System.currentTimeMillis() - before
        call.respond(
            Response(
                QueryStatus.fromLatency(latency),
                latency.toInt(),
                statistics,
                1,
                total = 1
            )
        )
    }
}

/**
 * Insertion rate is represented by insertions/hour
 */
fun getInsertionRate(start: Long = snippetCollection.find().ascendingSort(Snippet::insertionDate).first()?.insertionDate ?: 0, end: Long = System.currentTimeMillis()): Float {
    val totalSnippetsInTimePeriod = snippetCollection.find(
        and(
            Snippet::insertionDate gte start,
            Snippet::insertionDate lte end
        )
    ).count()

    return totalSnippetsInTimePeriod / ((end - start) / 1000 / 60 / 60f)
}