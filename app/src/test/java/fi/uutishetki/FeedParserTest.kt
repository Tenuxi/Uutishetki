package fi.uutishetki

import org.junit.Assert.*
import org.junit.Test

class FeedParserTest {
    private fun feed(items: String) = """<rss version="2.0" xmlns:media="http://search.yahoo.com/mrss/"><channel>$items</channel></rss>"""
    private fun item(extra: String = "", link: String = "https://example.org/a") = """
        <item><title><![CDATA[Otsikko &amp; asia]]></title><link>$link</link><guid>123</guid>
        <pubDate>Thu, 05 Nov 2026 07:34:50 +0200</pubDate>$extra</item>
    """
    @Test fun parsesSanomaEnclosureAndCleansTeaser() {
        val news = FeedParser.parse(feed(item("""<description><![CDATA[<p>Lyhyt <b>kuvaus</b> &amp; asia.</p>]]></description><category>TV &amp; elokuvat</category><enclosure url="https://example.org/image.jpg" type="image/jpeg"/>""")), Source.IS).single()
        assertEquals("Otsikko & asia", news.title)
        assertEquals("Lyhyt kuvaus & asia.", news.teaser)
        assertEquals("https://example.org/image.jpg", news.image)
        assertEquals(setOf(Topic.ENTERTAINMENT), news.topics)
        assertEquals(1793856890000L, news.published)
    }
    @Test fun supportsRegionalMultipleCategoriesAndNoImage() {
        val news = FeedParser.parse(feed(item("<category>Kotimaa</category><category>Urheilu</category><category>Uusimaa</category>")), Source.UUSIMAA).single()
        assertEquals(setOf(Topic.DOMESTIC, Topic.SPORT), news.topics)
        assertNull(news.teaser); assertNull(news.image)
    }
    @Test fun mediaDescriptionDoesNotBecomeTeaser() {
        val news = FeedParser.parse(feed(item("""<media:content url="https://example.org/photo.jpg" type="image/jpeg"><media:description>Kuvateksti</media:description></media:content>""")), Source.IL).single()
        assertEquals("https://example.org/photo.jpg", news.image)
        assertNull(news.teaser)
    }
    @Test fun stableIdsDeduplicateWithinSource() {
        val a = FeedParser.parse(feed(item() + item(link = "https://example.org/a?utm_source=rss")), Source.HS)
        assertEquals(1, a.size)
        assertNotEquals(a.single().id, FeedParser.parse(feed(item()), Source.IS).single().id)
    }
    @Test fun fallbackIdentityIgnoresTrackingQuery() {
        val xml = feed(item() + item(link = "https://example.org/a?utm_source=rss")).replace("<guid>123</guid>", "")
        assertEquals(1, FeedParser.parse(xml, Source.IL).size)
    }
    @Test fun rejectsUnsafeLinksAndNonFeeds() {
        assertTrue(FeedParser.parse(feed(item(link = "javascript:alert(1)")), Source.IS).isEmpty())
        assertThrows(IllegalArgumentException::class.java) { FeedParser.parse("<html>Access denied</html>", Source.IS) }
        assertNull(FeedParser.date("not a date"))
    }
    @Test fun combinedFiltersKeepUnknownOnlyInAllTopics() {
        val sport = FeedParser.parse(feed(item("<category>Urheilu</category>")), Source.IL).single()
        val unknown = sport.copy(id = "unknown", topics = emptySet())
        val other = sport.copy(id = "other", source = Source.IS)
        val all = listOf(sport, unknown, other)
        assertEquals(listOf(sport), all.filtered(Source.IL, Topic.SPORT))
        assertEquals(listOf(sport, unknown), all.filtered(Source.IL, null))
        assertEquals(all, all.filtered(null, null))
        assertTrue(all.filtered(Source.HS, Topic.SPORT).isEmpty())
    }
    @Test fun teaserIsBoundedAndEmptyIsAbsent() {
        val teaser = FeedParser.teaser("pitkä sana ".repeat(60))!!
        assertTrue(teaser.length <= 180); assertTrue(teaser.endsWith("…"))
        assertNull(FeedParser.teaser("<p> </p>"))
    }
}
