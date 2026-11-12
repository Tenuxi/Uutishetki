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
    private val located = MutableStateFlow<WeatherPlace?>(null)
    val currentLocation = located.asStateFlow()
    val locationMessage = MutableStateFlow<String?>(null)
    val offerPermission = MutableStateFlow(!preferences.contains("settings") && !preferences.getBoolean("permissionExplained", false))
    fun permissionExplained() { preferences.edit().putBoolean("permissionExplained", true).apply(); offerPermission.value = false }
    fun onBackground() { located.value = null }
    suspend fun refreshLocation() {
        if (!selected.value.automatic) { located.value = null; locationMessage.value = null; return }
        if (!location.hasPermission()) { located.value = null; locationMessage.value = "Sijaintilupaa ei ole. Sää käyttää varapaikkakuntaa; liikennetiedotteet odottavat sijaintia tai käsivalintaa."; return }
        val result = try { location.current() } catch (e: CancellationException) { throw e } catch (_: Exception) { null }
        located.value = result
        locationMessage.value = if (result == null) "Sijaintia ei saatu riittävän tarkasti. Sää käyttää varapaikkakuntaa. Voit valita paikkakunnan käsin." else null
    }
    private val location = ForegroundLocation(app)
    private val api = WeatherApi((app as NewsApplication).client)
    private val storedData = WeatherData.decode(preferences.getString("latest", null))?.takeIf { it.origin != null }
    private val displayed = MutableStateFlow(WeatherUiState(data = storedData, requestedPlace = storedData?.place ?: selected.value.weatherPlace(null),
        origin = storedData?.origin ?: selected.value.origin(null)))
    val state = displayed.asStateFlow()
    private val found = MutableStateFlow(PlaceSearch())
    val search = found.asStateFlow()
    private var searchJob: Job? = null
    private var lastAttempt = 0L
    private val mutex = Mutex()

    fun hasLocationPermission() = location.hasPermission()
    fun enabled(value: Boolean) = save(selected.value.copy(enabled = value, visibilityExplicit = true))
    fun automatic() = save(selected.value.copy(automatic = true))
    fun manualMode() = save(selected.value.copy(automatic = false))
    fun choose(place: WeatherPlace) { save(selected.value.copy(automatic = false, manual = place)); found.value = PlaceSearch() }
    fun permissionDenied() { permissionExplained(); located.value = null; refreshToken.value++ }
    private fun save(value: WeatherSettings) {
        preferences.edit().putString("settings", value.encode()).remove("latest").apply()
        displayed.value = WeatherUiState(requestedPlace = value.weatherPlace(null), origin = value.origin(null))
        lastAttempt = 0
        located.value = null
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
            val locationSnapshot = located.value
            val place = config.weatherPlace(locationSnapshot).rounded()
            val origin = config.origin(locationSnapshot)
            if (displayed.value.origin != origin || displayed.value.requestedPlace != place || (displayed.value.data != null && displayed.value.data?.place != place)) {
                displayed.value = WeatherUiState(requestedPlace = place, origin = origin); lastAttempt = 0
            }
            val previous = displayed.value.data
            if (previous != null && previous.isFresh(now) && forecastDates(now).all { date -> previous.forecast.any { it.date == date } } && now - previous.fetchedAt in 0 until WEATHER_INTERVAL) return
            if (now - lastAttempt in 0 until WEATHER_RETRY) return
            lastAttempt = now
            displayed.value = displayed.value.copy(loading = true, error = null)
            val data = api.weather(place).copy(origin = origin)
            ensureActiveContext()
            if (config == selected.value) {
                preferences.edit().putString("latest", data.encode()).apply()
                displayed.value = WeatherUiState(data = data, requestedPlace = place, origin = origin)
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
