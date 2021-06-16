package com.adamratzman.crawling.crawlers.au

import com.adamratzman.crawling.Crawler
import com.adamratzman.sourcing.NewsSourceEnum
import com.adamratzman.sourcing.NewsSourceEnum.NEWS_COM_AU

val aus1 = listOf(
    Crawler(
        NEWS_COM_AU, "https://www.news.com.au/content-feeds/", "https://www.news.com.au/more-information/rss-feeds",
        headlineName = "RSS news.com.au — Australia’s #1 news site | World News"
    ),
    Crawler(
        NewsSourceEnum.THE_SYDNEY_MORNING_HERALD, "https://www.smh.com.au/rss/", "https://www.smh.com.au/rssheadlines",
        headlineName = "Sydney Morning Herald - National"
    ),
    Crawler(
        NewsSourceEnum.THE_AGE, "https://www.theage.com.au/rss/", "https://www.theage.com.au/rssheadlines",
        headlineName = "The Age - National"
    ),
    Crawler(NewsSourceEnum.HERALD_SUN, "/rss", "http://www.heraldsun.com.au/help-rss", headlineName = "Herald Sun"),
    Crawler(
        NewsSourceEnum.THE_DAILY_TELEGRAPH_AU, "/rss", "https://www.dailytelegraph.com.au/help-rss",
        headlineName = "Breaking News &#124; Daily Telegraph"
    ),
    Crawler(
        NewsSourceEnum.THE_WEST_AUSTRALIA,
        "/rss",
        "https://thewest.com.au/rss-feeds",
        headlineName = "The West Australian"
    ),
    Crawler(
        NewsSourceEnum.ABC_NEWS_AU, "/rss.xml", "https://adamratzman.com/uploads/octagon/rss-urls.pdf",
        headlineName = "Top Stories", globalHeadline = true, initialFeedUrls = listOf(
            "http://www.abc.net.au/news/feed/46182/rss.xml",
            "http://www.abc.net.au/local/rss/sydney/news.xml",
            "http://www.abc.net.au/news/feed/1534/rss.xml",
            "http://www.abc.net.au/radionational/feed/3727018/rss.xml",
            "http://www.abc.net.au/radionational/feed/2884582/rss.xml",
            "http://www.abc.net.au/news/feed/51892/rss.xml",
            "http://www.abc.net.au/news/feed/45924/rss.xml"
        )
    ),
    Crawler(NewsSourceEnum.ABC_NEWS_AU_RURAL, "https://www.abc.net.au/news/feed", "https://www.abc.net.au/news/rural/rss/"),
    Crawler(
        NewsSourceEnum.NINE_NEWS,
        "",
        "",
        headlineName = "9News",
        initialFeedUrls = listOf("https://www.9news.com.au/rss")
    )
)