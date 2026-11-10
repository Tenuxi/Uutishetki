package fi.uutishetki

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

fun forecastDates(now: Long): List<LocalDate> = listOf(helsinkiDay(now).plusDays(1), helsinkiDay(now).plusDays(2))
data class DailyForecast(val date: LocalDate, val code: Int?, val low: Double?, val high: Double?, val rain: Int?) {
    fun json() = JSONObject().put("date", date.toString()).put("code", code ?: JSONObject.NULL)
        .put("low", low ?: JSONObject.NULL).put("high", high ?: JSONObject.NULL).put("rain", rain ?: JSONObject.NULL)
}
private fun JSONObject.number(key: String, range: ClosedFloatingPointRange<Double>): Double? =
    optDouble(key).takeIf { it.isFinite() && it in range }
fun decodeForecast(array: JSONArray?): List<DailyForecast> = if (array == null) emptyList() else
    (0 until array.length()).mapNotNull { i -> runCatching {
        val j = array.getJSONObject(i)
        DailyForecast(LocalDate.parse(j.getString("date")), j.number("code", 0.0..99.0)?.toInt(),
            j.number("low", -100.0..70.0), j.number("high", -100.0..70.0), j.number("rain", 0.0..100.0)?.toInt())
    }.getOrNull() }.distinctBy { it.date }.sortedBy { it.date }

fun parseDailyForecast(root: JSONObject): List<DailyForecast> = runCatching {
    require(root.getString("timezone") == "Europe/Helsinki")
    val daily = root.getJSONObject("daily")
    val units = root.getJSONObject("daily_units")
    require(units.getString("time") == "iso8601")
    val dates = daily.getJSONArray("time")
    fun value(key: String, unit: String, index: Int, range: ClosedFloatingPointRange<Double>): Double? {
        if (units.optString(key) != unit) return null
        return daily.optJSONArray(key)?.optDouble(index)?.takeIf { it.isFinite() && it in range }
    }
    (0 until dates.length()).mapNotNull { i -> runCatching {
        val low = value("temperature_2m_min", "°C", i, -100.0..70.0)
        val high = value("temperature_2m_max", "°C", i, -100.0..70.0)
        val validRange = low == null || high == null || low <= high
        DailyForecast(LocalDate.parse(dates.getString(i)), value("weather_code", "wmo code", i, 0.0..99.0)?.toInt(),
            low.takeIf { validRange }, high.takeIf { validRange }, value("precipitation_probability_max", "%", i, 0.0..100.0)?.toInt())
    }.getOrNull() }.distinctBy { it.date }.sortedBy { it.date }
}.getOrDefault(emptyList())
