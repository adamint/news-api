package com.adamratzman.crawling.crawlers.norway

import com.adamratzman.crawling.Crawler
import com.adamratzman.sourcing.NewsSourceEnum

val no1 = listOf(
        Crawler(NewsSourceEnum.AFTENPOSTEN,"", "", initialFeedUrls = listOf("https://www.aftenposten.no/rss"))
)