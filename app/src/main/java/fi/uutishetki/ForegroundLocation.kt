package fi.uutishetki

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.*

class ForegroundLocation(private val context: Context) {
    fun hasPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // Permission is checked immediately before the one-shot request; revocation is handled by caller.
    suspend fun current(): WeatherPlace? {
        if (!hasPermission()) return null
        val manager = context.getSystemService(LocationManager::class.java) ?: return null
        val provider = listOf("network", "fused", "gps").firstOrNull { name ->
            name in manager.allProviders && runCatching { manager.isProviderEnabled(name) }.getOrDefault(false)
        } ?: return null
        val location = withTimeoutOrNull(15_000) {
            suspendCancellableCoroutine<android.location.Location?> { continuation ->
                val cancellation = CancellationSignal()
                continuation.invokeOnCancellation { cancellation.cancel() }
                LocationManagerCompat.getCurrentLocation(manager, provider, cancellation, ContextCompat.getMainExecutor(context)) {
                    if (continuation.isActive) continuation.resume(it)
                }
            }
        } ?: return null
        val age = (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) / 1_000_000
        if (age !in 0..5 * 60_000 || !location.hasAccuracy() || location.accuracy > 25_000) return null
        val point = WeatherPlace("Nykyinen sijainti (paikkakunta ei saatavilla)", location.latitude, location.longitude).rounded()
        val name = locality(point)
        return if (name.isNullOrBlank()) point else point.copy(name = name.take(200))
    }
    @Suppress("DEPRECATION")
    private suspend fun locality(point: WeatherPlace): String? {
        if (!Geocoder.isPresent()) return null
        return try {
            withTimeoutOrNull(4_000) {
                val geocoder = Geocoder(context, Locale.forLanguageTag("fi"))
                val addresses = if (Build.VERSION.SDK_INT >= 33) suspendCancellableCoroutine<List<android.location.Address>> { continuation ->
                    geocoder.getFromLocation(point.latitude, point.longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<android.location.Address>) {
                            if (continuation.isActive) continuation.resume(addresses)
                        }
                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) continuation.resume(emptyList<android.location.Address>())
                        }
                    })
                } else runInterruptible(Dispatchers.IO) { geocoder.getFromLocation(point.latitude, point.longitude, 1).orEmpty() }
                addresses.firstOrNull()?.let { it.locality ?: it.subAdminArea }
            }
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { null }
    }
}
