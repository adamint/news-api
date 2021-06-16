package com.adamratzman.db

import com.adamratzman.sourcing.FeedInformation
import com.adamratzman.sourcing.NewsFeed
import com.adamratzman.sourcing.NewsSourceData
import com.adamratzman.sourcing.NewsSourceEnum
import com.adamratzman.sourcing.Snippet
import com.mongodb.client.MongoCollection
import org.litote.kmongo.*

val mongoClient = KMongo.createClient(
    "mongodb://newsapi:newsapi@localhost"
)

val database = mongoClient.getDatabase("news-api")

val sourceCollection: MongoCollection<NewsSourceData> = database.getCollection()
val feedInfoCollection = database.getCollection<FeedInformation>()
val newsFeedCollection = database.getCollection<NewsFeed>()
val snippetCollection = database.getCollection<Snippet>()