package fi.uutishetki

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class NewSourcesAndWeatherPanelTest {
    // Synthetic text, same RSS field structure as the verified publisher feeds.
    private fun xml(category: String = "", guid: String = "https://www.stara.fi/?p=123") = """
        <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/"><channel><item>
        <title>Testiotsikko</title><link>https://example.org/uutiset/testi/</link>
        <guid isPermaLink="false">$guid</guid><pubDate>Wed, 11 Nov 2026 16:30:00 +0000</pubDate>
        <description><![CDATA[<p>Lyhyt <b>kuvaus</b>.</p>]]></description>$category
        <content:encoded><![CDATA[<p>Koko teksti, jota ei näytetä.</p>]]></content:encoded>
        </item></channel></rss>
    """
    @Test fun staraUsesCategoriesInsteadOfBlanketEntertainment() {
        val entertainment = FeedParser.parse(xml("<category>Viihdeuutiset</category><category>Mirella</category>"), Source.STARA).single()
        assertEquals(setOf(Topic.ENTERTAINMENT), entertainment.topics)
        assertEquals("Lyhyt kuvaus.", entertainment.teaser)
        assertNull(entertainment.image)
        assertEquals(Instant.parse("2026-11-11T16:30:00Z").toEpochMilli(), entertainment.published)
        assertTrue(FeedParser.parse(xml("<category>Uutiset</category><category>Matkailu</category>"), Source.STARA).single().topics.isEmpty())
        assertEquals(setOf(Topic.DIGITAL), FeedParser.parse(xml("<category>Digi</category>"), Source.STARA).single().topics)
        val sports = FeedParser.parse(xml("<category>Urheilu</category>"), Source.STARA)
        assertTrue(sports.filtered(Selection()).isEmpty())
    }
    @Test fun pelaajaVerifiedGameFeedIsDigitalWithoutCategoryFields() {
        val feed = feeds.single { it.source == Source.PELAAJA }
        assertEquals("https://www.pelaaja.fi/feed/", feed.url)
        val article = FeedParser.parse(xml(guid = "https://www.pelaaja.fi/?post_type=uutiset&amp;p=123"), feed.source, feed.topic).single()
        assertEquals(setOf(Topic.DIGITAL), article.topics)
        assertNull(article.image)
        assertEquals("Lyhyt kuvaus.", article.teaser)
        assertEquals(1, mergeNews(listOf(article, article.copy(url = article.url + "?utm_source=rss"))).size)
        val all = SelectionCodec.decode(SelectionCodec.encode(Selection()))
        assertEquals(listOf(article), listOf(article).filtered(all))
        val restricted = SelectionCodec.decode(SelectionCodec.encode(Selection(Source.IL)))
        assertTrue(listOf(article).filtered(restricted).isEmpty())
        assertEquals(Source.STARA, SelectionCodec.decode(SelectionCodec.encode(Selection(Source.STARA))).source)
        assertEquals(Source.PELAAJA, SelectionCodec.decode(SelectionCodec.encode(Selection(Source.PELAAJA))).source)
    }
    @Test fun originTracksActualFallbackManualAndLocatedChoicesAndCache() {
        val manual = WeatherPlace("Tampere", 61.5, 23.76)
        assertEquals(WeatherOrigin.DEFAULT, WeatherSettings().origin(null))
        assertEquals(WeatherOrigin.CURRENT, WeatherSettings().origin(manual))
        assertEquals(WeatherOrigin.MANUAL, WeatherSettings(automatic = false, manual = manual).origin(HELSINKI))
        assertEquals(WeatherOrigin.DEFAULT, WeatherSettings(automatic = false).origin(null))
        val data = WeatherData(manual, 7.2, 0, false, 100, 200, origin = WeatherOrigin.CURRENT)
        assertEquals(data, WeatherData.decode(data.encode()))
        assertEquals(WeatherSymbol.MOON, weatherSymbol(data.code, data.isDay))
    }
    @Test fun timestampsUseObservationAndHelsinkiDayBoundary() {
        fun time(s: String) = Instant.parse(s).toEpochMilli()
        val observed = time("2026-11-11T21:30:00Z")
        assertEquals("Päivitetty 23.30", weatherUpdatedLabel(observed, time("2026-11-11T21:59:59Z")))
        assertEquals("Päivitetty 11.11.2026 23.30", weatherUpdatedLabel(observed, time("2026-11-11T22:00:00Z")))
        assertEquals("Päivitetty 0.30", weatherUpdatedLabel(time("2026-07-01T21:30:00Z"), time("2026-07-01T22:00:00Z")))
        assertEquals("+7°", weatherDegrees(7.2))
        assertEquals("-3°", weatherDegrees(-2.8))
        assertEquals("0°", weatherDegrees(0.1))
        assertEquals("—", weatherDegrees(null))
    }
}
