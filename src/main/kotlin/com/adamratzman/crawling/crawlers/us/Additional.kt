package com.adamratzman.crawling.crawlers.us

import com.adamratzman.crawling.Crawler
import com.adamratzman.sourcing.NewsSourceEnum

val us2 = listOf(
        Crawler(NewsSourceEnum.THE_CONVERSATION_US, "https://theconversation.com/us/", "https://theconversation.com/us/feeds"),
        Crawler(NewsSourceEnum.ARS_TECHNICA, "feeds.arstechnica.com", "https://arstechnica.com/rss-feeds/")
)