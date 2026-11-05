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
        assertEquals(listOf(sport), all.filtered(Selection(Source.IL, setOf(Topic.SPORT))))
        assertEquals(listOf(sport, unknown), all.filtered(Selection(Source.IL, Topic.entries.toSet())))
        assertEquals(all, all.filtered(Selection(topics = Topic.entries.toSet())))
        assertTrue(all.filtered(Selection(Source.HS, setOf(Topic.SPORT))).isEmpty())
    }
    @Test fun teaserIsBoundedAndEmptyIsAbsent() {
        val teaser = FeedParser.teaser("pitkä sana ".repeat(60))!!
        assertTrue(teaser.length <= 180); assertTrue(teaser.endsWith("…"))
        assertNull(FeedParser.teaser("<p> </p>"))
    }
    @Test fun digitalMetadataAndVerifiedFeedContext() {
        for (category in listOf("digiuutiset", "Digitoday", "Mobiili", "Tietoturva")) {
            assertTrue(Topic.DIGITAL in FeedParser.parse(feed(item("<category>$category</category>")), Source.IS).single().topics)
        }
        val genericTest = feed(item("<category>Testit</category>"))
        assertTrue(FeedParser.parse(genericTest, Source.IS).single().topics.isEmpty())
        assertEquals(setOf(Topic.DIGITAL), FeedParser.parse(genericTest, Source.IS, Topic.DIGITAL).single().topics)
    }
    @Test fun generalAndDigitalFeedCopiesKeepIdentityAndTopics() {
        val xml = feed(item("<category>Testit</category>"))
        val general = FeedParser.parse(xml, Source.IS).single()
        val digital = FeedParser.parse(xml, Source.IS, Topic.DIGITAL).single()
        assertEquals(general.id, digital.id)
        for (copies in listOf(listOf(general, digital), listOf(digital, general))) {
            assertEquals(setOf(Topic.DIGITAL), mergeNews(copies).single().topics)
        }
    }

}
