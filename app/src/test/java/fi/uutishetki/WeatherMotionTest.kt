package fi.uutishetki

import org.junit.Assert.*
import org.junit.Test

class WeatherMotionTest {
    @Test fun precipitationUsesApiTypeWithoutInventingSleet() {
        listOf(61, 63, 65, 66, 67, 80, 81, 82).forEach { assertEquals(WeatherMotion.RAIN, weatherMotion(it)) }
        listOf(51, 53, 55, 56, 57).forEach { assertEquals(WeatherMotion.DRIZZLE, weatherMotion(it)) }
        listOf(71, 73, 75, 77, 85, 86).forEach { assertEquals(WeatherMotion.SNOW, weatherMotion(it)) }
        listOf(95, 96, 99).forEach { assertEquals(WeatherMotion.THUNDER, weatherMotion(it)) }
    }
    @Test fun clearCloudAndFogStayDistinct() {
        assertEquals(WeatherMotion.CLEAR, weatherMotion(0))
        assertEquals(WeatherMotion.CLEAR, weatherMotion(1))
        assertEquals(WeatherMotion.CLOUD, weatherMotion(2))
        assertEquals(WeatherMotion.CLOUD, weatherMotion(3))
        assertEquals(WeatherMotion.FOG, weatherMotion(45))
        assertEquals(WeatherMotion.FOG, weatherMotion(48))
    }
    @Test fun missingAndUnknownWeatherDoNotAnimate() {
        assertEquals(WeatherMotion.STILL, weatherMotion(null))
        assertEquals(WeatherMotion.STILL, weatherMotion(999))
        val old = WeatherData(HELSINKI, 5.0, 63, true, 0, 0)
        val state = WeatherUiState(data = old, now = 100_000_000)
        assertEquals(WeatherMotion.STILL, weatherMotion(state.current?.code))
    }
}
