package fi.uutishetki

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ForecastTest {
    private val now = Instant.parse("2026-11-10T08:00:00Z").toEpochMilli()
    private fun response() = JSONObject("""{
      "timezone":"Europe/Helsinki","current_units":{"time":"iso8601","temperature_2m":"°C"},
      "current":{"time":"2026-11-10T10:00","temperature_2m":4,"weather_code":3,"is_day":1},
      "daily_units":{"time":"iso8601","temperature_2m_min":"°C","temperature_2m_max":"°C","weather_code":"wmo code","precipitation_probability_max":"%"},
      "daily":{"time":["2026-11-10","2026-11-11","2026-11-12"],"temperature_2m_min":[0,-2,1],"temperature_2m_max":[4,3,5],"weather_code":[3,71,61],"precipitation_probability_max":[5,80,null]}}
    """)
    @Test fun tomorrowAndDayAfterUseDatesAndSurviveCache() {
        val data = WeatherParser.current(response().toString(),HELSINKI,now)
        assertEquals(data,WeatherData.decode(data.encode()))
        val dates = forecastDates(now)
        assertEquals(listOf(LocalDate.of(2026,11,11),LocalDate.of(2026,11,12)),dates)
        val tomorrow = data.forecast.single { it.date == dates.first() }
        assertEquals(-2.0,tomorrow.low!!,0.001)
        assertEquals(80,tomorrow.rain)
        assertNull(data.forecast.last().rain)
        assertEquals(listOf(LocalDate.of(2026,11,12),LocalDate.of(2026,11,13)),forecastDates(Instant.parse("2026-11-10T22:01:00Z").toEpochMilli()))
    }
    @Test fun missingDailyAndBadUnitsDoNotDiscardCurrentWeatherOrInventValues() {
        val j = response()
        j.remove("daily")
        assertTrue(WeatherParser.current(j.toString(),HELSINKI,now).forecast.isEmpty())
        val bad = response()
        bad.getJSONObject("daily_units").put("temperature_2m_min","°F")
        bad.getJSONObject("daily").getJSONArray("precipitation_probability_max").put(1,999)
        val day = parseDailyForecast(bad)[1]
        assertNull(day.low)
        assertNull(day.rain)
    }
    @Test fun legacyChoicesPreservedAndMissingVisibilityDefaultsOn() {
        assertTrue(WeatherSettings.decode(null).enabled)
        val implicit = WeatherSettings.decode("""{"automatic":false,"manual":{"name":"Oulu","lat":65,"lon":25}}""")
        assertTrue(implicit.enabled)
        assertFalse(implicit.visibilityExplicit)
        assertTrue(WeatherSettings.decode("""{"enabled":false,"visibilityExplicit":false}""").enabled)
        val off = WeatherSettings.decode("""{"enabled":false,"automatic":true}""")
        assertFalse(off.enabled)
        assertTrue(off.visibilityExplicit)
        assertEquals(off, WeatherSettings.decode(off.encode()))
        val chosen = WeatherSettings(enabled=false,visibilityExplicit=true)
        assertEquals(chosen,WeatherSettings.decode(chosen.encode()))
    }
}
