package fi.uutishetki

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class OpeningAndDaysTest {
    @Test fun openingScrollIsConsumedOnceAndExternalReturnIsNotOpening() {
        val p = OpeningPolicy()
        val first = requireNotNull(p.started())
        assertTrue(p.consumeScroll(first))
        assertFalse(p.consumeScroll(first))
        p.externalLaunch(); p.stopped()
        assertNull(p.started())
        p.stopped()
        val second = requireNotNull(p.started())
        assertTrue(second > first)
        assertTrue(p.consumeScroll(second))
    }
    @Test fun gestureAndExternalLaunchCancelPendingScroll() {
        for (external in listOf(false, true)) {
            val p = OpeningPolicy()
            val first = requireNotNull(p.started())
            if (external) p.externalLaunch() else p.userInteracted()
            assertFalse(p.consumeScroll(first))
        }
    }
    @Test fun processRestoredFromBrowserDoesNotBecomeNewOpening() {
        val restored = OpeningPolicy()
        restored.restoreExternalReturn()
        assertNull(restored.started())
        restored.stopped()
        assertNotNull(restored.started())
    }
    @Test fun repeatedStartsOrOldFetchCompletionDoNotResetReading() {
        val p = OpeningPolicy()
        val first = requireNotNull(p.started())
        assertNull(p.started()) // configuration change / no real background transition
        p.stopped()
        val second = requireNotNull(p.started())
        assertFalse(p.consumeScroll(first))
        assertTrue(p.consumeScroll(second))
    }
    @Test fun dateGroupsUseHelsinkiAndLabelsChangeAtMidnight() {
        val millis = Instant.parse("2026-11-08T22:30:00Z").toEpochMilli()
        assertEquals(LocalDate.of(2026,11,9), helsinkiDay(millis))
        val item = FeedEntry.Article(News("one",Source.IL,"Title","https://example.com",null,null,millis,emptySet()))
        val group = groupFeedDays(listOf(item)).single()
        assertEquals("Tänään", dayLabel(group.date,LocalDate.of(2026,11,9)))
        assertEquals("Eilen", dayLabel(group.date,LocalDate.of(2026,11,10)))
        assertEquals("9.11.2026", dayLabel(group.date,LocalDate.of(2026,11,11)))
        assertTrue(groupFeedDays(emptyList()).isEmpty())
    }
    @Test fun newsAndTrafficShareGroupsInPublicationOrder() {
        val time = Instant.parse("2026-11-10T08:00:00Z").toEpochMilli()
        val news = News("n", Source.IL, "Uutinen", "https://example.com", null, null, time, emptySet())
        val traffic = TrafficNotice("t", 1, time + 1000, time + 2000, time, null, "Tiedote", null, "", null,
            TrafficGeometry(points = listOf(GeoPoint(60.0, 25.0))), null)
        val groups = groupFeedDays(mergeFeed(listOf(news, news.copy(id = "old", published = time - 86400_000)), listOf(NearbyTraffic(traffic, 1.0))))
        assertEquals(2, groups.size)
        assertEquals(2, groups.first().entries.size)
        assertTrue(groups.first().entries.first() is FeedEntry.Traffic)
        assertEquals(LocalDate.of(2026,11,9), groups.last().date)
    }
    @Test fun summerTimeAndUnknownDatesAreNotUtcDaysOrEpochDates() {
        assertEquals(LocalDate.of(2026,7,2),helsinkiDay(Instant.parse("2026-07-01T21:01:00Z").toEpochMilli()))
        val unknown = FeedEntry.Article(News("one",Source.IL,"Title","https://example.com",null,null,null,emptySet()))
        assertNull(groupFeedDays(listOf(unknown)).single().date)
        assertEquals("Julkaisuaika ei saatavilla",dayLabel(null,LocalDate.of(2026,11,10)))
    }
}
