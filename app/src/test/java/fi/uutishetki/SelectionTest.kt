package fi.uutishetki

import org.junit.Assert.*
import org.junit.Test

class SelectionTest {
    private fun news(id: String, vararg topics: Topic, source: Source = Source.IL) =
        News(id, source, id, "https://example.org/$id", null, null, null, topics.toSet())

    @Test fun firstLaunchDefaultsExcludeEvenMixedSportsAndIncludeUnknownAndDigi() {
        val selection = SelectionCodec.decode(null)
        assertEquals(Topic.entries.toSet() - Topic.SPORT, selection.topics)
        assertNull(selection.source)
        assertEquals("Kaikki paitsi urheilu", selection.summary)
        val digi = news("digi", Topic.DIGITAL)
        val unknown = news("unknown")
        val mixed = news("sport", Topic.DOMESTIC, Topic.SPORT)
        assertEquals(listOf(digi, unknown), listOf(digi, unknown, mixed).filtered(selection))
        assertEquals(listOf(digi, unknown, mixed), listOf(digi, unknown, mixed).filtered(selection.toggle(Topic.SPORT)))
    }
    @Test fun multipleTopicsUseOrAndSourceUsesAnd() {
        val domestic = news("home", Topic.DOMESTIC)
        val digi = news("digi", Topic.DIGITAL)
        val foreign = news("foreign", Topic.WORLD)
        val otherSource = news("other", Topic.DIGITAL, source = Source.IS)
        assertEquals(listOf(domestic, digi), listOf(domestic, digi, foreign, otherSource)
            .filtered(Selection(Source.IL, setOf(Topic.DOMESTIC, Topic.DIGITAL))))
    }
    @Test fun emptySelectionRemainsEmptyAfterPersistenceRoundTrip() {
        val saved = SelectionCodec.encode(Selection(Source.HS, emptySet()))
        val restored = SelectionCodec.decode(saved)
        assertEquals(Selection(Source.HS, emptySet()), restored)
        assertTrue(listOf(news("unknown"), news("home", Topic.DOMESTIC)).filtered(restored).isEmpty())
    }
    @Test fun customSelectionRoundTripDoesNotRestoreDefaults() {
        val custom = Selection(Source.IS, setOf(Topic.DIGITAL, Topic.SPORT))
        assertEquals(custom, SelectionCodec.decode(SelectionCodec.encode(custom)))
        assertEquals(custom.toggle(Topic.SPORT), Selection(Source.IS, setOf(Topic.DIGITAL)))
        assertEquals(Selection(), SelectionCodec.decode(SelectionCodec.encode(Selection())))
    }
    @Test fun unknownSavedCategoryIsNotMistakenForAllTopics() {
        assertTrue(SelectionCodec.decode("IS|REMOVED_CATEGORY").topics.isEmpty())
    }
}
