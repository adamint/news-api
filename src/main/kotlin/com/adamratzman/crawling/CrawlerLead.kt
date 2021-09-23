package com.adamratzman.crawling

import com.adamratzman.crawling.crawlers.au.aus1
import com.adamratzman.crawling.crawlers.au.aus2
import com.adamratzman.crawling.crawlers.italy.ita1
import com.adamratzman.crawling.crawlers.norway.no1
import com.adamratzman.crawling.crawlers.saudi.saud1
import com.adamratzman.crawling.crawlers.us.us1
import com.adamratzman.crawling.crawlers.us.us2
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MINUTES
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private val executor = Executors.newScheduledThreadPool(2)

class CrawlerLead {
    private val crawlers: List<Crawler> = getCrawlerLists().flatten()

    init {
        executor.scheduleWithFixedDelay({
            runBlocking {
                crawlers.forEach { crawler ->
                    launch {
                        try {
                            println("Populating feed for ${crawler.source.readableName}")
                            crawler.populateFeeds()
                        } catch (e: Exception) {
                            //e.printStackTrace()
                        }
                    }
                }
            }
        }, 0, 10, MINUTES)

        executor.scheduleWithFixedDelay({
            runBlocking {
                try {
                    updateSnippets()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 0, 2, MINUTES)
    }
}


fun getCrawlerLists(): List<List<Crawler>> = listOf(
    us1,
    us2,
    aus1,
    aus2,
    no1,
    ita1,
    saud1
)
