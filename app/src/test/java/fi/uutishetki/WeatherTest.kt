package fi.uutishetki

import org.junit.Assert.*
import org.junit.Test

class WeatherTest {
    private val place = WeatherPlace("Helsinki", 60.1699, 24.9384)
    private val now = 1_800_000_000_000L
    private fun response(temp: String = "4.2", seconds: Long = now / 1000, unit: String = "°C") =
        """{"current_units":{"temperature_2m":"$unit","time":"unixtime"},"current":{"time":$seconds,"temperature_2m":$temp,"weather_code":3,"is_day":1}}"""

    @Test fun settingsDefaultOffAndPersistManualAndAutomatic() {
        assertEquals(WeatherSettings(), WeatherSettings.decode(null))
        assertEquals(WeatherSettings(), WeatherSettings.decode("broken"))
        for (automatic in listOf(false, true)) {
            val settings = WeatherSettings(true, automatic, place)
            assertEquals(settings, WeatherSettings.decode(settings.encode()))
            assertEquals(settings.copy(enabled = false), WeatherSettings.decode(settings.copy(enabled = false).encode()))
        }
    }
    @Test fun manualWorksWithoutPermissionButAutomaticRequiresPermission() {
        assertNull(WeatherSettings(true, false, place).unavailableReason(false))
        assertNotNull(WeatherSettings(true, true, place).unavailableReason(false))
        assertNull(WeatherSettings(true, true).unavailableReason(true))
        assertNotNull(WeatherSettings(true).unavailableReason(true))
        assertNotNull(WeatherSettings().unavailableReason(true))
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
