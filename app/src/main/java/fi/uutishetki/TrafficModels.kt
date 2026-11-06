package fi.uutishetki

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import kotlin.math.*

const val TRAFFIC_INTERVAL = 5 * 60_000L
const val TRAFFIC_URL = "https://tie.digitraffic.fi/api/traffic-message/v2/traffic-announcements"
fun trafficFresh(fetchedAt: Long, now: Long, failed: Boolean) = !failed && fetchedAt > 0 && now - fetchedAt in 0 until TRAFFIC_INTERVAL
val TRAFFIC_RADII = listOf(5, 10, 25, 50)

data class TrafficSettings(val enabled: Boolean = true, val radius: Int = 10) {
    fun encode() = JSONObject().put("enabled", enabled).put("radius", radius).toString()
    companion object {
        fun decode(raw: String?) = runCatching {
            val j = JSONObject(requireNotNull(raw))
            TrafficSettings(j.getBoolean("enabled"), j.getInt("radius").also { require(it in TRAFFIC_RADII) })
        }.getOrDefault(TrafficSettings())
    }
}
data class GeoPoint(val lat: Double, val lon: Double) {
    init { require(lat.isFinite() && lon.isFinite() && lat in -90.0..90.0 && lon in -180.0..180.0) }
}
// Great-circle distances to the nearest point of each segment, in kilometres.
object GeoDistance {
    private const val R = 6371.0088
    private fun rad(x: Double) = Math.toRadians(x)
    private fun angle(a: GeoPoint, b: GeoPoint): Double {
        val h = sin(rad(b.lat - a.lat) / 2).pow(2) + cos(rad(a.lat)) * cos(rad(b.lat)) * sin(rad(b.lon - a.lon) / 2).pow(2)
        return 2 * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }
    fun point(a: GeoPoint, b: GeoPoint) = angle(a, b) * R
    private fun bearing(a: GeoPoint, b: GeoPoint) = atan2(sin(rad(b.lon - a.lon)) * cos(rad(b.lat)),
        cos(rad(a.lat)) * sin(rad(b.lat)) - sin(rad(a.lat)) * cos(rad(b.lat)) * cos(rad(b.lon - a.lon)))
    fun segment(p: GeoPoint, a: GeoPoint, b: GeoPoint): Double {
        val length = angle(a, b)
        if (length < 1e-12) return point(p, a)
        val delta = angle(a, p)
        val direction = bearing(a, p) - bearing(a, b)
        val along = atan2(sin(delta) * cos(direction), cos(delta))
        return if (along in 0.0..length) abs(asin((sin(delta) * sin(direction)).coerceIn(-1.0, 1.0))) * R
            else min(point(p, a), point(p, b))
    }
    fun line(p: GeoPoint, line: List<GeoPoint>) = line.zipWithNext().minOf { (a, b) -> segment(p, a, b) }
    private fun inside(p: GeoPoint, ring: List<GeoPoint>): Boolean {
        var inside = false
        for ((a, b) in ring.zipWithNext()) {
            if ((a.lat > p.lat) != (b.lat > p.lat) && p.lon < (b.lon - a.lon) * (p.lat - a.lat) / (b.lat - a.lat) + a.lon) inside = !inside
        }
        return inside
    }
    fun polygon(p: GeoPoint, rings: List<List<GeoPoint>>): Double =
        if (inside(p, rings.first()) && rings.drop(1).none { inside(p, it) }) 0.0 else rings.minOf { line(p, it) }
}
data class TrafficGeometry(val points: List<GeoPoint> = emptyList(), val lines: List<List<GeoPoint>> = emptyList(),
    val polygons: List<List<List<GeoPoint>>> = emptyList()) {
    fun distance(place: WeatherPlace): Double {
        val p = GeoPoint(place.latitude, place.longitude)
        return (points.map { GeoDistance.point(p, it) } + lines.map { GeoDistance.line(p, it) } + polygons.map { GeoDistance.polygon(p, it) }).min()
    }
    companion object {
        fun parse(j: JSONObject): TrafficGeometry {
            val c = j.getJSONArray("coordinates")
            fun point(a: JSONArray) = GeoPoint(a.getDouble(1), a.getDouble(0))
            fun line(a: JSONArray) = (0 until a.length()).map { point(a.getJSONArray(it)) }.also { require(it.size >= 2) }
            fun polygon(a: JSONArray) = (0 until a.length()).map { line(a.getJSONArray(it)).also { ring -> require(ring.size >= 4 && ring.first() == ring.last()) } }.also { require(it.isNotEmpty()) }
            return when (j.getString("type")) {
                "Point" -> TrafficGeometry(points = listOf(point(c)))
                "MultiPoint" -> TrafficGeometry(points = (0 until c.length()).map { point(c.getJSONArray(it)) }.also { require(it.isNotEmpty()) })
                "LineString" -> TrafficGeometry(lines = listOf(line(c)))
                "MultiLineString" -> TrafficGeometry(lines = (0 until c.length()).map { line(c.getJSONArray(it)) }.also { require(it.isNotEmpty()) })
                "Polygon" -> TrafficGeometry(polygons = listOf(polygon(c)))
                "MultiPolygon" -> TrafficGeometry(polygons = (0 until c.length()).map { polygon(c.getJSONArray(it)) }.also { require(it.isNotEmpty()) })
                else -> error("Tuntematon geometria")
            }
        }
    }
}
data class TrafficNotice(val id: String, val version: Int, val published: Long, val updated: Long, val start: Long,
    val end: Long?, val title: String, val location: String?, val description: String, val sender: String?,
    val geometry: TrafficGeometry, val sourceLink: String?) {
    fun active(now: Long) = start <= now && (end == null || now < end)
}
data class NearbyTraffic(val notice: TrafficNotice, val distance: Double)
fun nearbyTraffic(notices: List<TrafficNotice>, place: WeatherPlace?, radius: Int, now: Long): List<NearbyTraffic> =
    if (place == null) emptyList() else notices.filter { it.active(now) }.map { NearbyTraffic(it, it.geometry.distance(place)) }
        .filter { it.distance <= radius }.sortedByDescending { it.notice.published }

object TrafficParser {
    private fun time(s: String) = Instant.parse(s).toEpochMilli()
    fun parse(raw: String): List<TrafficNotice> {
        val root = JSONObject(raw)
        require(root.getString("type") == "FeatureCollection")
        time(root.getString("dataUpdatedTime"))
        val features = root.getJSONArray("features")
        // Select newest version BEFORE checking status: a terminal version must remove the old card.
        val latest = (0 until features.length()).map { features.getJSONObject(it) }
            .groupBy { it.getJSONObject("properties").getString("situationId") }
            .values.map { copies -> copies.maxWith(compareBy<JSONObject> { it.getJSONObject("properties").getInt("version") }
                .thenBy { time(it.getJSONObject("properties").getString("versionTime")) }) }
        return latest.mapNotNull { feature -> runCatching {
            val p = feature.getJSONObject("properties")
            if (p.getString("situationType") != "traffic announcement" || p.getString("trafficAnnouncementType") !in
                setOf("general", "preliminary accident report", "accident report", "unconfirmed observation")) return@runCatching null
            val a = p.getJSONArray("announcements").let { rows -> (0 until rows.length()).map { rows.getJSONObject(it) }.first { it.getString("language") == "fi" } }
            if (a.optString("earlyClosing") in setOf("closed", "canceled")) return@runCatching null
            val geometryJson = feature.getJSONObject("geometry")
            if (a.optJSONObject("locationDetails")?.optJSONObject("areaLocation") != null && geometryJson.getString("type") in setOf("Point", "MultiPoint")) return@runCatching null
            val duration = a.getJSONObject("timeAndDuration")
            val description = buildList {
                a.optJSONArray("features")?.let { rows -> for (i in 0 until rows.length()) {
                    val f = rows.getJSONObject(i)
                    add(listOfNotNull(f.text("name"), f.text("quantity"), f.text("unit")).joinToString(" "))
                    f.text("description")?.let { add(it) }
                } }
                a.text("comment")?.let { add(it) }
                a.text("additionalInformation")?.let { add(it) }
            }.joinToString("\n")
            // Only links actually supplied in the source, no constructed event URLs.
            val link = Regex("https://[^\\s<>]+").find(a.text("additionalInformation").orEmpty())?.value?.let(::httpsUrl)
            TrafficNotice(p.getString("situationId"), p.getInt("version"), time(p.getString("releaseTime")), time(p.getString("versionTime")),
                time(duration.getString("startTime")), duration.text("endTime")?.let(::time), a.getString("title"),
                a.optJSONObject("location")?.text("description"), description, a.text("sender"), TrafficGeometry.parse(geometryJson), link)
        }.getOrNull() }
    }
    private fun JSONObject.text(key: String): String? = if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
}
