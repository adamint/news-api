package com.adamratzman.sourcing

import java.util.Locale
import kotlinx.serialization.Serializable

@Serializable
data class SourceData(val name: String, val _id: String, val rssBase: String, val rssList: String,
                      val headlineName: String?, val globalHeadline: Boolean, val headline: Boolean,
                      val country: Country = Country.USA, val newsSource: NewsSourceEnum = NewsSourceEnum.values().first { _id == it._id })

@Serializable
data class NewsSourceData(
    val readableName: String,
    val _id: String,
    val country: Country = Country.USA,
    val parent: NewsSourceEnum? = null
)

enum class NewsSourceEnum(
    val readableName: String,
    __id: String,
    val country: Country = Country.USA,
    val parent: NewsSourceEnum? = null
) {
    // Qatar
    AL_JAZEERA("Al Jazeera", "al_jazeera"),

    // USA
    THE_WASHINGTON_POST("The Washington Post", "the_washington_post"),
    THE_NEW_YORK_TIMES("The New York Times", "nyt"),
    THE_WALL_STREET_JOURNAL("The Wall Street Journal", "wsj"),
    CNN("CNN", "cnn"),
    CNN_MONEY("CNN Money", "money", parent = CNN),
    ABC_NEWS("ABC News", "abc_news"),
    AL_JAZEERA_ENGLISH("Al Jazeera English", "english", parent = AL_JAZEERA),

    THE_CONVERSATION("The Conversation", "the_conversation"),

    THE_CONVERSATION_US("The Conversation (US)", "us", parent = THE_CONVERSATION),
    ARS_TECHNICA("Ars Technica", "ars_technica"),

    // Australia
    NEWS_COM_AU("News.com.au", "news_com_au", Country.AUSTRALIA),
    THE_SYDNEY_MORNING_HERALD("The Sydney Morning Herald", "sydney_morning_herald", Country.AUSTRALIA),
    THE_AGE("The Age", "the_age", Country.AUSTRALIA),
    HERALD_SUN("Herald Sun", "herald_sun", Country.AUSTRALIA),
    THE_DAILY_TELEGRAPH_AU("The Daily Telegraph (AU)", "the_daily_telegraph_au", Country.AUSTRALIA),
    THE_WEST_AUSTRALIA("The West (AU)", "the_west_au", Country.AUSTRALIA),
    ABC_NEWS_AU("ABC News (AU)", "abc_news_au", Country.AUSTRALIA),
    ABC_NEWS_AU_RURAL("ABC News Rural (AU)", "rural", parent = ABC_NEWS_AU),
    NINE_NEWS("9News", "nine_news"),

    THE_CONVERSATION_AU("The Conversation (AU)", "au", Country.AUSTRALIA, parent = THE_CONVERSATION),

    // Norway
    AFTENPOSTEN("Aftenposten", "aftenposten", Country.NORWAY),

    // Italy
    ANSA("ANSA", "ansa", Country.ITALY),

    // Saudi Arabia
    ARGAAM("Argaam", "argaam", Country.SAUDI_ARABIA)
    ;

    override fun toString() = readableName
    val _id: String = parent?.let { "${parent._id}_$__id" } ?: "source_$__id"

    fun getCategory(): SourceCategory = when (this._id) {
        "wsj" -> SourceCategory.BUSINESS
        "cnn_money" -> SourceCategory.BUSINESS

        else -> SourceCategory.GENERAL
    }

    fun getSnippetId() = this._id.uppercase(Locale.getDefault())

    fun asData() = NewsSourceData(readableName, _id, country, parent)
}

enum class SourceCategory(val id: String) { GENERAL("general"), BUSINESS("business"), ENTERTAINMENT("entertainment") }

enum class Country(val abbrev: String) {
    USA("us"),
    AUSTRALIA("au"),
    NORWAY("no"),
    ITALY("it"),
    SAUDI_ARABIA("sa"),

}
