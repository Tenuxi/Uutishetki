package fi.uutishetki

import java.net.URI
import java.security.MessageDigest
import java.time.ZonedDateTime
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

enum class Source(val label: String, val feed: String) {
    IL("Iltalehti", "https://www.iltalehti.fi/rss/rss.xml"),
    IS("Ilta-Sanomat", "https://www.is.fi/rss/tuoreimmat.xml"),
    UUSIMAA("Yle Uusimaa", "https://yle.fi/rss/t/18-147345/fi"),
    HAME("Yle Häme", "https://yle.fi/rss/t/18-138727/fi"),
    HS("Helsingin Sanomat", "https://www.hs.fi/rss/tuoreimmat.xml")
}
enum class Topic(val label: String, val aliases: Set<String>) {
    DOMESTIC("Kotimaa", setOf("kotimaa", "politiikka", "kaupunki")),
    WORLD("Ulkomaat", setOf("ulkomaat")),
    ECONOMY("Talous", setOf("talous", "taloussanomat", "raha", "pörssi", "sijoittaminen")),
    SPORT("Urheilu", setOf("urheilu", "jääkiekko", "jalkapallo", "formula 1", "f1", "hiihto", "ralli", "yleisurheilu", "koripallo", "tennis", "salibandy", "pesäpallo", "jaakiekko", "nhl", "smliiga", "formulat")),
    ENTERTAINMENT("Viihde", setOf("viihde", "tv & elokuvat", "tv-ja-elokuvat", "popkulttuuri", "musiikki", "kuninkaalliset", "tv-ja-leffat"))
}
data class News(
    val id: String, val source: Source, val title: String, val url: String,
    val teaser: String?, val image: String?, val published: Long?, val topics: Set<Topic>
)
fun List<News>.filtered(source: Source?, topic: Topic?) = filter {
    (source == null || it.source == source) && (topic == null || topic in it.topics)
}
fun httpsUrl(value: String): String? = runCatching {
    URI(value.trim()).takeIf { it.scheme == "https" && !it.host.isNullOrBlank() && it.userInfo == null }?.toString()
}.getOrNull()

object FeedParser {
    fun parse(xml: String, source: Source): List<News> {
        // XML mode never resolves external entities; only feed fields are read, never article pages.
        val doc = Jsoup.parse(xml, "", Parser.xmlParser())
        require(doc.selectFirst("rss > channel") != null) { "Vastaus ei ole RSS-syöte" }
        return doc.select("channel > item").take(200).mapNotNull { item ->
            fun field(name: String) = item.children().firstOrNull { it.tagName() == name }?.text().orEmpty()
            val url = httpsUrl(field("link")) ?: return@mapNotNull null
            val title = plain(field("title")).take(500).takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val key = field("guid").ifBlank { URI(url).let { URI(it.scheme, it.authority, it.path, null, null).toString() } }
            val id = source.name + ":" + MessageDigest.getInstance("SHA-256")
                .digest(key.toByteArray()).joinToString("") { "%02x".format(it) }
            val categories = item.children().filter { it.tagName() == "category" }.map { plain(it.text()).lowercase(Locale.ROOT) }
            val topics = Topic.entries.filter { topic -> categories.any { it in topic.aliases } }.toSet()
            val image = item.children().firstNotNullOfOrNull { el ->
                if ((el.tagName() == "enclosure" && el.attr("type").startsWith("image/")) ||
                    (el.tagName() in setOf("media:content", "media:thumbnail") &&
                        (el.attr("type").startsWith("image/") || el.attr("medium") == "image" || el.tagName() == "media:thumbnail")))
                    httpsUrl(el.attr("url")) else null
            }
            News(id, source, title, url, teaser(field("description")), image, date(field("pubDate")), topics)
        }.distinctBy { it.id }
    }
    fun plain(value: String) = Jsoup.parse(value).text().replace(Regex("\\s+"), " ").trim()
    fun teaser(value: String): String? {
        val text = plain(value)
        if (text.isBlank()) return null
        if (text.length <= 180) return text
        val cut = text.lastIndexOf(' ', 177).takeIf { it > 100 } ?: 177
        return text.take(cut).trimEnd() + "…"
    }
    fun date(value: String): Long? = runCatching {
        ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
    }.getOrElse { runCatching { Instant.parse(value).toEpochMilli() }.getOrNull() }
}
