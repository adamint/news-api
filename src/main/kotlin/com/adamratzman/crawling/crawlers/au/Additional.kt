package com.adamratzman.crawling.crawlers.au

import com.adamratzman.crawling.Crawler
import com.adamratzman.sourcing.NewsSourceEnum.THE_CONVERSATION_AU

val aus2 = listOf(
    Crawler(THE_CONVERSATION_AU, "https://theconversation.com/au/", "https://theconversation.com/au/feeds")
)
