package fi.uutishetki

import org.junit.Assert.*
import org.junit.Test

class WeatherTest {
    private val place = WeatherPlace("Helsinki", 60.1699, 24.9384)
    private val now = 1_800_000_000_000L
    private fun response(temp: String = "4.2", seconds: Long = now / 1000, unit: String = "°C") =
        """{"current_units":{"temperature_2m":"$unit","time":"unixtime"},"current":{"time":$seconds,"temperature_2m":$temp,"weather_code":3,"is_day":1}}"""

    @Test fun settingsDefaultOnAndPersistManualAndAutomatic() {
        assertEquals(WeatherSettings(), WeatherSettings.decode(null))
        assertEquals(WeatherSettings(), WeatherSettings.decode("broken"))
        for (automatic in listOf(false, true)) {
            val settings = WeatherSettings(true, automatic, place)
            assertEquals(settings, WeatherSettings.decode(settings.encode()))
            assertEquals(settings.copy(enabled = false, visibilityExplicit = true), WeatherSettings.decode(settings.copy(enabled = false, visibilityExplicit = true).encode()))
        }
    }
    @Test fun fallbackWeatherNeverBecomesTrafficLocation() {
        val settings = WeatherSettings()
        assertTrue(settings.enabled)
        assertTrue(settings.automatic)
        assertEquals(HELSINKI, settings.weatherPlace(null))
        assertNull(settings.trafficPlace(null))
        assertEquals(place, settings.weatherPlace(place))
        assertEquals(place, settings.trafficPlace(place))
        val manual = WeatherSettings(true, false, place)
        assertEquals(place, manual.weatherPlace(HELSINKI))
        assertEquals(place, manual.trafficPlace(HELSINKI))
        assertEquals(place, manual.weatherPlace(null))
        assertEquals(HELSINKI, settings.copy(manual = place).weatherPlace(null))
        assertNull(settings.copy(manual = place).trafficPlace(null))
    }
    @Test fun previousExplicitOffAndManualChoicesSurviveUpgrade() {
        val old = """{"enabled":false,"automatic":false,"manual":{"name":"Oulu","lat":65.01,"lon":25.47}}"""
        val settings = WeatherSettings.decode(old)
        assertFalse(settings.enabled)
        assertFalse(settings.automatic)
        assertEquals("Oulu", settings.weatherPlace(null).name)
        assertEquals(settings, WeatherSettings.decode(settings.encode()))
    }
    @Test fun validWeatherAndCacheRoundTrip() {
        val data = WeatherParser.current(response(), place, now)
        assertEquals("+4°", data.temperatureLabel)
        assertEquals(data, WeatherData.decode(data.encode()))
        assertEquals(data, WeatherUiState(data, now = now).current)
        assertEquals(WeatherSymbol.CLOUD, weatherSymbol(data.code, data.isDay))
    }
    @Test fun staleFutureAndFailedWeatherNeverLooksCurrent() {
        val data = WeatherParser.current(response(), place, now)
        assertNull(WeatherUiState(data, error = "Verkkovirhe", now = now).current)
        assertNull(WeatherUiState(data, now = now + 31 * 60_000).current)
        assertFalse(data.copy(observedAt = now - 61 * 60_000).isFresh(now))
        assertFalse(data.copy(observedAt = now + 1000).isFresh(now))
        assertFalse(data.copy(fetchedAt = now + 1000).isFresh(now))
        assertNull(WeatherData.decode("broken"))
    }
    @Test fun invalidResponsesAreRejected() {
        for (raw in listOf(response("null"), response("999"), response(unit = "°F"),
            response(seconds = now / 1000 - 3601), response(seconds = now / 1000 + 1),
            "{}", """{"error":true}""")) {
            assertTrue(raw, runCatching { WeatherParser.current(raw, place, now) }.isFailure)
        }
    }
    @Test fun searchOnlyReturnsFinnishPopulatedPlaces() {
        val places = WeatherParser.places("""{"results":[
            {"name":"Helsinki","country_code":"FI","feature_code":"PPLC","latitude":60.17,"longitude":24.94},
            {"name":"Airport","country_code":"FI","feature_code":"AIRP","latitude":60,"longitude":24},
            {"name":"Elsewhere","country_code":"SE","feature_code":"PPL","latitude":60,"longitude":24},
            {"name":"Invalid","country_code":"FI","feature_code":"PPL","latitude":999,"longitude":24}] }""")
        assertEquals(listOf(WeatherPlace("Helsinki", 60.17, 24.94)), places)
        assertTrue(WeatherParser.places("{}").isEmpty())
    }
    @Test fun coordinatesRoundedAndUnknownConditionsNotInvented() {
        assertEquals(60.17, place.rounded().latitude, 0.00001)
        assertEquals(24.94, place.rounded().longitude, 0.00001)
        assertEquals(WeatherSymbol.UNKNOWN, weatherSymbol(999, true))
        assertEquals(WeatherSymbol.MOON, weatherSymbol(0, false))
    }
}
