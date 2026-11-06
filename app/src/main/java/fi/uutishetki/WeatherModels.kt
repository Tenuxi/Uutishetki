package fi.uutishetki

import org.json.JSONObject
import kotlin.math.round
import kotlin.math.roundToInt

private const val MINUTE = 60_000L
const val WEATHER_INTERVAL = 15 * MINUTE
const val WEATHER_RETRY = 5 * MINUTE

data class WeatherPlace(val name: String, val latitude: Double, val longitude: Double) {
    init {
        require(name.isNotBlank() && name.length <= 200)
        require(latitude.isFinite() && latitude in -90.0..90.0)
        require(longitude.isFinite() && longitude in -180.0..180.0)
    }
    fun rounded() = copy(latitude = round(latitude * 100) / 100, longitude = round(longitude * 100) / 100)
    fun json() = JSONObject().put("name", name).put("lat", latitude).put("lon", longitude)
    companion object {
        fun fromJson(json: JSONObject) = WeatherPlace(json.getString("name"), json.getDouble("lat"), json.getDouble("lon"))
    }
}
data class WeatherSettings(val enabled: Boolean = false, val automatic: Boolean = false, val manual: WeatherPlace? = null) {
    fun unavailableReason(hasPermission: Boolean): String? = when {
        !enabled -> "Sää ei ole käytössä."
        automatic && !hasPermission -> "Sijaintilupa puuttuu. Valitse automaattinen sijainti uudelleen tai paikkakunta käsin."
        !automatic && manual == null -> "Valitse paikkakunta sääasetuksista."
        else -> null
    }
    fun encode(): String = JSONObject().put("enabled", enabled).put("automatic", automatic)
        .put("manual", manual?.json() ?: JSONObject.NULL).toString()
    companion object {
        fun decode(raw: String?): WeatherSettings = runCatching {
            val json = JSONObject(requireNotNull(raw))
            WeatherSettings(json.getBoolean("enabled"), json.getBoolean("automatic"),
                json.optJSONObject("manual")?.let(WeatherPlace::fromJson))
        }.getOrDefault(WeatherSettings())
    }
}
data class WeatherData(val place: WeatherPlace, val temperature: Double, val code: Int,
    val isDay: Boolean, val observedAt: Long, val fetchedAt: Long) {
    // Both clocks are checked: neither an old API observation nor an old cache looks current.
    fun isFresh(now: Long) = now - observedAt in 0..60 * MINUTE && now - fetchedAt in 0..30 * MINUTE
    val temperatureLabel: String get() = temperature.roundToInt().let { if (it > 0) "+$it°" else "$it°" }
    fun encode() = JSONObject().put("place", place.json()).put("temperature", temperature).put("code", code)
        .put("day", isDay).put("observed", observedAt).put("fetched", fetchedAt).toString()
    companion object {
        fun decode(raw: String?): WeatherData? = runCatching {
            val j = JSONObject(requireNotNull(raw))
            val temp = j.getDouble("temperature")
            require(temp.isFinite() && temp in -100.0..70.0)
            WeatherData(WeatherPlace.fromJson(j.getJSONObject("place")), temp, j.getInt("code"), j.getBoolean("day"),
                j.getLong("observed"), j.getLong("fetched"))
        }.getOrNull()
    }
}
data class WeatherUiState(val data: WeatherData? = null, val loading: Boolean = false,
    val error: String? = null, val now: Long = System.currentTimeMillis()) {
    val current: WeatherData? get() = data?.takeIf { error == null && it.isFresh(now) }
}
object WeatherParser {
    fun current(raw: String, place: WeatherPlace, now: Long): WeatherData {
        val root = JSONObject(raw)
        require(!root.optBoolean("error"))
        val units = root.getJSONObject("current_units")
        require(units.getString("temperature_2m") == "°C" && units.getString("time") == "unixtime")
        val current = root.getJSONObject("current")
        val temp = current.getDouble("temperature_2m")
        require(temp.isFinite() && temp in -100.0..70.0)
        val seconds = current.getLong("time")
        require(seconds in 1..Long.MAX_VALUE / 1000)
        val code = current.getInt("weather_code")
        val day = current.getInt("is_day")
        require(day in 0..1)
        return WeatherData(place, temp, code, day == 1, seconds * 1000, now).also { require(it.isFresh(now)) }
    }
    fun places(raw: String): List<WeatherPlace> {
        val root = JSONObject(raw)
        require(!root.optBoolean("error"))
        val results = root.optJSONArray("results") ?: return emptyList()
        return (0 until results.length()).mapNotNull { index -> runCatching {
            val j = results.getJSONObject(index)
            // Only populated places; do not offer islands or airports as municipalities.
            if (j.getString("country_code") != "FI" || !j.getString("feature_code").startsWith("PPL")) return@runCatching null
            val label = listOf(j.getString("name"), j.optString("admin1")).filter { it.isNotBlank() }.distinct().joinToString(", ")
            WeatherPlace(label, j.getDouble("latitude"), j.getDouble("longitude"))
        }.getOrNull() }.distinct()
    }
}
enum class WeatherSymbol { SUN, MOON, CLOUD, RAIN, SNOW, THUNDER, FOG, UNKNOWN }
fun weatherSymbol(code: Int, day: Boolean): WeatherSymbol = when (code) {
    0, 1 -> if (day) WeatherSymbol.SUN else WeatherSymbol.MOON
    2, 3 -> WeatherSymbol.CLOUD
    45, 48 -> WeatherSymbol.FOG
    51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherSymbol.RAIN
    71, 73, 75, 77, 85, 86 -> WeatherSymbol.SNOW
    95, 96, 99 -> WeatherSymbol.THUNDER
    else -> WeatherSymbol.UNKNOWN
}
fun weatherDescription(code: Int): String = when (code) {
    0 -> "Selkeää"; 1 -> "Enimmäkseen selkeää"; 2 -> "Puolipilvistä"; 3 -> "Pilvistä"
    45, 48 -> "Sumua"; 51, 53, 55 -> "Tihkusadetta"; 56, 57 -> "Jäätävää tihkua"
    61, 63, 65 -> "Vesisadetta"; 66, 67 -> "Jäätävää sadetta"
    71, 73, 75, 77 -> "Lumisadetta"; 80, 81, 82 -> "Sadekuuroja"; 85, 86 -> "Lumikuuroja"
    95 -> "Ukkosta"; 96, 99 -> "Ukkosta ja rakeita"; else -> "Säätilan kuvaus ei saatavilla"
}
