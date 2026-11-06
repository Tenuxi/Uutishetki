package fi.uutishetki

import android.app.Application
import android.util.AtomicFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import org.json.JSONObject
import java.io.File

data class TrafficUiState(val nearby: List<NearbyTraffic> = emptyList(), val loading: Boolean = false,
    val message: String? = null, val checkedAt: Long = 0, val place: WeatherPlace? = null, val radius: Int = 10, val unavailable: Boolean = false)

class TrafficViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("traffic", 0)
    private val selected = MutableStateFlow(TrafficSettings.decode(prefs.getString("settings", null)))
    val settings = selected.asStateFlow()
    private val displayed = MutableStateFlow(TrafficUiState())
    val state = displayed.asStateFlow()
    private val http = JsonHttp((app as NewsApplication).client)
    private val cache = AtomicFile(File(app.filesDir, "traffic-cache.json"))
    private var notices = emptyList<TrafficNotice>()
    private var fetchedAt = 0L
    private var nextAttempt = 0L
    private var failed = false
    private val mutex = Mutex()
    private val restored = viewModelScope.async(Dispatchers.IO) {
        runCatching {
            require(cache.baseFile.length() <= 12L * 1024 * 1024)
            val j = JSONObject(cache.openRead().bufferedReader().use { it.readText() })
            j.getLong("fetchedAt") to TrafficParser.parse(j.getString("raw"))
        }.getOrNull()
    }
    private var cacheLoaded = false
    fun enabled(value: Boolean) = save(selected.value.copy(enabled = value))
    fun radius(value: Int) { require(value in TRAFFIC_RADII); save(selected.value.copy(radius = value)) }
    private fun save(value: TrafficSettings) {
        prefs.edit().putString("settings", value.encode()).apply()
        selected.value = value
        if (!value.enabled) displayed.value = TrafficUiState()
    }
    suspend fun refresh(place: WeatherPlace?) {
        if (!mutex.tryLock()) return
        try {
            if (!cacheLoaded) {
                restored.await()?.let { (time, data) -> fetchedAt = time; notices = data }
                cacheLoaded = true
            }
            if (!selected.value.enabled) { displayed.value = TrafficUiState(); return }
            if (place == null) {
                displayed.value = TrafficUiState(message = "Odottaa sijaintia tai paikkakunnan valintaa. Helsinki on vain sään oletuspaikka.")
                return
            }
            val now = System.currentTimeMillis()
            if (now - fetchedAt !in 0 until TRAFFIC_INTERVAL && now >= nextAttempt) {
                displayed.value = TrafficUiState(loading = true, message = "Haetaan liikennetiedotteita…", checkedAt = fetchedAt)
                nextAttempt = now + TRAFFIC_INTERVAL
                try {
                    val raw = http.get(TRAFFIC_URL, 8L * 1024 * 1024)
                    val parsed = withContext(Dispatchers.Default) { TrafficParser.parse(raw) }
                    val time = System.currentTimeMillis()
                    withContext(Dispatchers.IO) {
                        val bytes = JSONObject().put("raw", raw).put("fetchedAt", time).toString().toByteArray()
                        val stream = cache.startWrite()
                        try { stream.write(bytes); cache.finishWrite(stream) } catch (e: Exception) { cache.failWrite(stream); throw e }
                    }
                    notices = parsed // Full snapshot replaces old entries, including records removed by source.
                    fetchedAt = time
                    failed = false
                } catch (e: CancellationException) { nextAttempt = 0; throw e }
                catch (_: Exception) { failed = true }
            }
            val current = System.currentTimeMillis()
            if (!trafficFresh(fetchedAt, current, failed)) {
                displayed.value = TrafficUiState(message = "Liikennetiedotteita ei voitu varmistaa ajantasaisiksi. Aiemmat tiedotteet on piilotettu.", checkedAt = fetchedAt, unavailable = true)
            } else {
                val list = withContext(Dispatchers.Default) { nearbyTraffic(notices, place, selected.value.radius, current) }
                displayed.value = TrafficUiState(nearby = list, checkedAt = fetchedAt, place = place, radius = selected.value.radius,
                    message = if (list.isEmpty()) "Ei voimassa olevia tiedotteita valitulla säteellä. Lista ei kata kaikkia liikenteen häiriöitä." else null)
            }
        } finally { mutex.unlock() }
    }
}
