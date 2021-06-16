package com.adamratzman.crawling.crawlers.italy

import com.adamratzman.crawling.Crawler
import com.adamratzman.sourcing.NewsSourceEnum

val ita1 = listOf(
    Crawler(
        NewsSourceEnum.ANSA, "rss.xml", "https://www.ansa.it/sito/static/ansa_rss.html",
        headlineName = "RSS di - ANSA.it"
    )
)