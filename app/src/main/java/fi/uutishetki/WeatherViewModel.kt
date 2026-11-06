package fi.uutishetki

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex

data class PlaceSearch(val results: List<WeatherPlace> = emptyList(), val busy: Boolean = false, val message: String? = null)

class WeatherViewModel(app: Application) : AndroidViewModel(app) {
    private val preferences = app.getSharedPreferences("weather", 0)
    private val selected = MutableStateFlow(WeatherSettings.decode(preferences.getString("settings", null)))
    val settings = selected.asStateFlow()
    val refreshToken = MutableStateFlow(0)
    private val location = ForegroundLocation(app)
    private val api = WeatherApi((app as NewsApplication).client)
    private val storedData = WeatherData.decode(preferences.getString("latest", null))
    private val displayed = MutableStateFlow(WeatherUiState(data = storedData))
    val state = displayed.asStateFlow()
    private val found = MutableStateFlow(PlaceSearch())
    val search = found.asStateFlow()
    private var searchJob: Job? = null
    private var lastAttempt = 0L
    private val mutex = Mutex()

    fun hasLocationPermission() = location.hasPermission()
    fun enabled(value: Boolean) = save(selected.value.copy(enabled = value))
    fun automatic() = save(selected.value.copy(enabled = true, automatic = true))
    fun manualMode() = save(selected.value.copy(automatic = false))
    fun choose(place: WeatherPlace) { save(selected.value.copy(enabled = true, automatic = false, manual = place)); found.value = PlaceSearch() }
    fun permissionDenied() {
        save(selected.value.copy(enabled = true, automatic = true))
        displayed.value = WeatherUiState(error = "Sijaintilupaa ei annettu. Voit valita paikkakunnan käsin.")
    }
    private fun save(value: WeatherSettings) {
        preferences.edit().putString("settings", value.encode()).remove("latest").apply()
        displayed.value = WeatherUiState()
        lastAttempt = 0
        selected.value = value
        refreshToken.value++
    }
    fun searchPlaces(query: String) {
        searchJob?.cancel()
        if (query.trim().length !in 3..80) { found.value = PlaceSearch(message = "Kirjoita vähintään kolme merkkiä."); return }
        searchJob = viewModelScope.launch {
            found.value = PlaceSearch(busy = true)
            try {
                val places = api.search(query)
                found.value = PlaceSearch(places, message = if (places.isEmpty()) "Paikkakuntaa ei löytynyt. Kokeile tarkempaa nimeä." else null)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { found.value = PlaceSearch(message = "Paikkakuntahaku ei onnistunut. Tarkista verkkoyhteys.") }
        }
    }
    fun dismissSearch() { searchJob?.cancel(); found.value = PlaceSearch() }

    // Called only inside repeatOnLifecycle(STARTED), never by composition or a background job.
    suspend fun refresh() {
        if (!mutex.tryLock()) return
        val config = selected.value
        try {
            val now = System.currentTimeMillis()
            displayed.value = displayed.value.copy(now = now)
            if (!config.enabled) return
            config.unavailableReason(location.hasPermission())?.let {
                displayed.value = WeatherUiState(error = it)
                return
            }
            val previous = displayed.value.data
            if (previous != null && previous.isFresh(now) && now - previous.fetchedAt in 0 until WEATHER_INTERVAL) return
            if (now - lastAttempt in 0 until WEATHER_RETRY) return
            lastAttempt = now
            displayed.value = displayed.value.copy(loading = true, error = null)
            val place = if (config.automatic) location.current() else config.manual
            if (place == null) {
                if (config == selected.value) displayed.value = displayed.value.copy(loading = false,
                    error = "Sijaintia ei saatu. Tarkista laitteen sijaintiasetus tai valitse paikkakunta käsin.")
                return
            }
            val data = api.weather(place)
            ensureActiveContext()
            if (config == selected.value) {
                preferences.edit().putString("latest", data.encode()).apply()
                displayed.value = WeatherUiState(data = data)
            }
        } catch (e: CancellationException) {
            // Retry when returning to the foreground instead of waiting after an interrupted request.
            lastAttempt = 0
            throw e
        } catch (_: Exception) {
            if (config == selected.value) displayed.value = displayed.value.copy(loading = false,
                error = "Säätä ei saatu päivitettyä. Tarkista verkkoyhteys tai yritä myöhemmin.", now = System.currentTimeMillis())
        } finally {
            displayed.value = displayed.value.copy(loading = false)
            mutex.unlock()
        }
    }
    private suspend fun ensureActiveContext() { currentCoroutineContext().ensureActive() }
}
