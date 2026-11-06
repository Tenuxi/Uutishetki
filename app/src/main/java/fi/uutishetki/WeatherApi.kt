package fi.uutishetki

import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class WeatherApi(client: OkHttpClient) {
    // No HTTP disk cache: coordinate URLs must not create a location history.
    private val http = JsonHttp(client)
    suspend fun weather(place: WeatherPlace): WeatherData {
        val rounded = place.rounded()
        val url = "https://api.open-meteo.com/v1/forecast".toHttpUrl().newBuilder()
            .addQueryParameter("latitude", rounded.latitude.toString()).addQueryParameter("longitude", rounded.longitude.toString())
            .addQueryParameter("current", "temperature_2m,weather_code,is_day")
            .addQueryParameter("timeformat", "unixtime").addQueryParameter("timezone", "Europe/Helsinki").build()
        return WeatherParser.current(http.get(url.toString()), rounded, System.currentTimeMillis())
    }
    suspend fun search(query: String): List<WeatherPlace> {
        require(query.trim().length in 3..80)
        val url = "https://geocoding-api.open-meteo.com/v1/search".toHttpUrl().newBuilder()
            .addQueryParameter("name", query.trim()).addQueryParameter("count", "20")
            .addQueryParameter("language", "fi").addQueryParameter("format", "json").addQueryParameter("countryCode", "FI").build()
        return WeatherParser.places(http.get(url.toString()))
    }
}

class JsonHttp(client: OkHttpClient) {
    private val client = client.newBuilder().cache(null).build()
    @Volatile private var blockedUntil = 0L
    suspend fun get(url: String, limit: Long = 256L * 1024): String = suspendCancellableCoroutine { continuation ->
        if (System.currentTimeMillis() < blockedUntil) {
            continuation.resumeWithException(IOException("Palvelun pyytämä odotusaika"))
            return@suspendCancellableCoroutine
        }
        val call = client.newCall(Request.Builder().url(url).header("User-Agent", "Uutishetki/${BuildConfig.VERSION_NAME}").header("Digitraffic-User", "Uutishetki/${BuildConfig.VERSION_NAME}").build())
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isActive) continuation.resumeWithException(e)
            }
            override fun onResponse(call: Call, response: Response) {
                val result = runCatching { response.use {
                    if (!it.isSuccessful) {
                        if (it.code == 429 || it.code == 503) {
                            val retry = it.header("Retry-After")
                            val now = System.currentTimeMillis()
                            val until = retry?.toLongOrNull()?.coerceIn(0, 7 * 24 * 3600)?.let { seconds -> now + seconds * 1000 }
                                ?: runCatching { java.time.ZonedDateTime.parse(retry, java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli() }.getOrNull()
                            blockedUntil = maxOf(now + 5 * 60_000, until ?: 0)
                        }
                        throw IOException("Tietopalvelu ei vastannut onnistuneesti")
                    }
                    val body = it.body ?: throw IOException("Tyhjä vastaus")
                    val source = body.source()
                    if (source.request(limit + 1)) throw IOException("Liian suuri vastaus")
                    source.readUtf8()
                } }
                if (continuation.isActive) result.fold({ continuation.resume(it) }, { continuation.resumeWithException(it) })
            }
        })
    }
}
