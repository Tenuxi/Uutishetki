package fi.uutishetki

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val HELSINKI_ZONE: ZoneId = ZoneId.of("Europe/Helsinki")
fun helsinkiDay(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(HELSINKI_ZONE).toLocalDate()
fun dayLabel(day: LocalDate?, today: LocalDate): String = when (day) {
    null -> "Julkaisuaika ei saatavilla"
    today -> "Tänään"
    today.minusDays(1) -> "Eilen"
    else -> day.format(DateTimeFormatter.ofPattern("d.M.yyyy"))
}
data class FeedDay(val date: LocalDate?, val entries: List<FeedEntry>)
fun groupFeedDays(entries: List<FeedEntry>): List<FeedDay> = entries.sortedByDescending { it.published }
    .groupBy { if (it.published > 0) helsinkiDay(it.published) else null }
    .map { (date, items) -> FeedDay(date, items) }

// Activity starts/stops, not every resume. Kept in ViewModel through configuration changes.
class OpeningPolicy {
    private var initial = true
    private var background = false
    private var external = false
    var generation = 0L; private set
    private var pending = false
    val externalPending: Boolean get() = external
    fun restoreExternalReturn() { initial = false; background = true; external = true; pending = false }
    fun externalLaunch() { external = true; userInteracted() }
    fun externalFailed() { external = false }
    fun stopped() { background = true }
    fun started(): Long? {
        val opening = initial || (background && !external)
        initial = false
        background = false
        external = false
        if (!opening) return null
        generation++
        pending = true
        return generation
    }
    fun userInteracted() { pending = false }
    fun consumeScroll(completedGeneration: Long): Boolean {
        if (!pending || completedGeneration != generation) return false
        pending = false
        return true
    }
}
