package fi.uutishetki

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class TrafficTest {
    private val now = Instant.parse("2026-11-06T08:00:00Z").toEpochMilli()
    private fun feature(version: Int = 1, status: String = "accident report", end: String? = null): JSONObject = JSONObject("""
        {"type":"Feature","geometry":{"type":"Point","coordinates":[24.94,60.17]},"properties":{
        "situationId":"test-1","situationType":"traffic announcement","trafficAnnouncementType":"$status","version":$version,
        "releaseTime":"2026-11-06T07:00:00Z","versionTime":"2026-11-06T07:30:00Z",
        "announcements":[{"language":"fi","title":"Testitiedote","location":{"description":"Testitie"},
        "features":[{"name":"Nopeusrajoitus","quantity":30,"unit":"km/h","description":"Noudata liikenteenohjausta."}],
        "comment":"Lähteen ohje sellaisenaan.","sender":"Testilähde","timeAndDuration":{"startTime":"2026-11-06T07:00:00Z"}}]}}
    """).also { if (end != null) it.getJSONObject("properties").getJSONArray("announcements").getJSONObject(0).getJSONObject("timeAndDuration").put("endTime", end) }
    private fun parse(vararg items: JSONObject): List<TrafficNotice> = TrafficParser.parse(
        """{"type":"FeatureCollection","dataUpdatedTime":"2026-11-06T08:00:00Z","features":[${items.joinToString(",")}] }""")
    private fun geometry(raw: String) = TrafficGeometry.parse(JSONObject(raw))

    @Test fun settingsDefaultOnTenKmAndOffSurvivesRestart() {
        assertEquals(TrafficSettings(true, 10), TrafficSettings.decode(null))
        for (radius in TRAFFIC_RADII) {
            val settings = TrafficSettings(false, radius)
            assertEquals(settings, TrafficSettings.decode(settings.encode()))
        }
    }
    @Test fun missingLocationNeverUsesHelsinkiForTraffic() {
        assertTrue(nearbyTraffic(parse(feature()), null, 50, now).isEmpty())
        assertEquals(1, nearbyTraffic(parse(feature()), HELSINKI, 5, now).size)
        assertTrue(nearbyTraffic(parse(feature()), WeatherPlace("Oulu",65.01,25.47),50,now).isEmpty())
    }
    @Test fun latestVersionReplacesOldAndKeepsOriginalPublicationTime() {
        val newer = feature(2).also { it.getJSONObject("properties").getJSONArray("announcements").getJSONObject(0).put("title","Uusi otsikko") }
        val notices = parse(newer, feature())
        assertEquals(1, notices.size)
        assertEquals("Uusi otsikko", notices.single().title)
        assertEquals(Instant.parse("2026-11-06T07:00:00Z").toEpochMilli(), notices.single().published)
        assertEquals(2, notices.single().version)
    }
    @Test fun terminalVersionsRemoveOldCardsBeforeFiltering() {
        for (status in listOf("ended", "retracted")) assertTrue(parse(feature(), feature(2, status)).isEmpty())
        assertTrue(nearbyTraffic(parse(feature(end="2026-11-06T08:00:00Z")), HELSINKI,10,now).isEmpty())
        assertEquals(1, nearbyTraffic(parse(feature(end="2026-11-06T08:00:01Z")), HELSINKI,10,now).size)
        assertTrue(nearbyTraffic(parse(feature()), HELSINKI,10,now - 2 * 3600_000).isEmpty())
    }
    @Test fun invalidOrMissingGeometryAndOtherDatasetsExcluded() {
        assertTrue(parse(feature().put("geometry", JSONObject.NULL)).isEmpty())
        assertTrue(parse(feature().also { it.getJSONObject("geometry").put("coordinates",org.json.JSONArray("[999,60]")) }).isEmpty())
        assertTrue(parse(feature().also { it.getJSONObject("properties").put("situationType","road work") }).isEmpty())
        assertTrue(parse(feature().also { it.getJSONObject("properties").getJSONArray("announcements").getJSONObject(0)
            .put("locationDetails",JSONObject("""{"areaLocation":{}}""")) }).isEmpty())
    }
    @Test fun nearestLineSegmentInsteadOfMidpointOrVertices() {
        val line = geometry("""{"type":"LineString","coordinates":[[24,60],[26,60]]}""")
        assertTrue(line.distance(WeatherPlace("Testi",60.01,24.1)) < 2)
        assertTrue(line.distance(WeatherPlace("Testi",60.1,24.1)) > 10)
        val multi = geometry("""{"type":"MultiLineString","coordinates":[[[20,60],[21,60]],[[24,60],[26,60]]]}""")
        assertEquals(line.distance(HELSINKI), multi.distance(HELSINKI),0.00001)
        assertEquals(0.0, GeoDistance.segment(GeoPoint(60.0,24.0),GeoPoint(60.0,24.0),GeoPoint(60.0,24.0)),0.00001)
    }
    @Test fun polygonIncludesInteriorButNotHoles() {
        val polygon = geometry("""{"type":"Polygon","coordinates":[[[24,60],[26,60],[26,62],[24,62],[24,60]],[[24.5,60.5],[25.5,60.5],[25.5,61.5],[24.5,61.5],[24.5,60.5]]]}""")
        assertEquals(0.0,polygon.distance(WeatherPlace("Sisällä",60.2,24.2)),0.00001)
        assertTrue(polygon.distance(WeatherPlace("Aukossa",61.0,25.0)) > 20)
        assertTrue(polygon.distance(WeatherPlace("Ulkona",59.9,25.0)) > 10)
        val multi = geometry("""{"type":"MultiPolygon","coordinates":[[[[24,60],[26,60],[26,62],[24,62],[24,60]]]]}""")
        assertEquals(0.0,multi.distance(HELSINKI),0.00001)
    }
    @Test fun radiusUsesGeometryAndStableCardKeyIsIndependentOfVersion() {
        val notices = parse(feature())
        val near = nearbyTraffic(notices,HELSINKI,10,now)
        assertEquals(1,mergeFeed(emptyList(),near).size)
        assertEquals(FeedEntry.Traffic(near.single()).key, FeedEntry.Traffic(near.single().copy(notice=near.single().notice.copy(version=2))).key)
        val news = News("a",Source.IL,"Artikkeli","https://example.com",null,null,now,emptySet())
        assertTrue(mergeFeed(listOf(news),near).first() is FeedEntry.Article)
        assertTrue(mergeFeed(emptyList(),near).single() is FeedEntry.Traffic)
    }
    @Test fun cachedTrafficHiddenAfterErrorOrExpiry() {
        assertTrue(trafficFresh(now, now + TRAFFIC_INTERVAL - 1, false))
        assertFalse(trafficFresh(now, now + TRAFFIC_INTERVAL, false))
        assertFalse(trafficFresh(now, now, true))
        assertFalse(trafficFresh(now, now - 1, false))
        assertFalse(trafficFresh(0, now, false))
    }
    @Test fun verifiedV2ResponseParsesWithoutChangingSourceText() {
        val raw = requireNotNull(javaClass.getResource("/traffic-v2-sample.json")).readText()
        val source = JSONObject(raw).getJSONArray("features").getJSONObject(0).getJSONObject("properties")
        val parsed = TrafficParser.parse(raw).single()
        assertEquals(source.getString("situationId"), parsed.id)
        assertEquals(source.getJSONArray("announcements").getJSONObject(0).getString("title"), parsed.title)
        assertNotNull(parsed.sourceLink)
    }
    @Test fun sourceInstructionsPreservedAndNoLinkInvented() {
        val notice = parse(feature()).single()
        assertTrue(notice.description.contains("Nopeusrajoitus 30 km/h"))
        assertTrue(notice.description.contains("Noudata liikenteenohjausta."))
        assertTrue(notice.description.contains("Lähteen ohje sellaisenaan."))
        assertNull(notice.sourceLink)
    }
}
