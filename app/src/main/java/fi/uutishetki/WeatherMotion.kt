package fi.uutishetki

enum class WeatherMotion { STILL, CLEAR, CLOUD, RAIN, DRIZZLE, SNOW, FOG, THUNDER }
fun weatherMotion(code: Int?): WeatherMotion = when (code) {
    0, 1 -> WeatherMotion.CLEAR
    2, 3 -> WeatherMotion.CLOUD
    45, 48 -> WeatherMotion.FOG
    51, 53, 55, 56, 57 -> WeatherMotion.DRIZZLE
    61, 63, 65, 66, 67, 80, 81, 82 -> WeatherMotion.RAIN
    71, 73, 75, 77, 85, 86 -> WeatherMotion.SNOW
    95, 96, 99 -> WeatherMotion.THUNDER
    else -> WeatherMotion.STILL
}
