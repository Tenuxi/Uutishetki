package fi.uutishetki

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// WMO has no separate sleet code in this API. Keep the supplied sleet art unmapped;
// never present a mixed-precipitation forecast that the API did not provide.
internal fun weatherArtwork(code: Int?, day: Boolean): Int = when (code) {
    0, 1 -> if (day) R.drawable.art_weather_clear_day else R.drawable.art_weather_clear_night
    2 -> if (day) R.drawable.art_weather_partly_day else R.drawable.art_weather_partly_night
    3 -> R.drawable.art_weather_cloudy
    45, 48 -> R.drawable.art_weather_fog
    51, 53, 55, 56, 57 -> R.drawable.art_weather_drizzle
    61, 63, 65, 66, 67, 80, 81, 82 -> R.drawable.art_weather_rain
    71, 73, 75, 77, 85, 86 -> R.drawable.art_weather_snow
    95, 96, 99 -> R.drawable.art_weather_thunder
    else -> R.drawable.art_weather_unknown
}
@Composable
fun WeatherArtwork(code: Int?, day: Boolean, modifier: Modifier) {
    Image(painterResource(weatherArtwork(code, day)), null, modifier)
}
@Composable
fun WeatherHero(data: WeatherData) {
    val palette = LocalAppSurfaces.current
    val accent = MaterialTheme.colorScheme.primary
    BoxWithConstraints(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
        .background(Brush.verticalGradient(listOf(palette.weatherCard, palette.weather)))) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width; val h = size.height
            val far = Path().apply {
                moveTo(0f, h * .83f); cubicTo(w*.2f,h*.37f,w*.4f,h*.95f,w*.7f,h*.62f)
                cubicTo(w*.85f,h*.5f,w*.95f,h*.65f,w,h*.56f); lineTo(w,h); lineTo(0f,h); close()
            }
            drawPath(far, accent.copy(alpha = .06f))
            val near = Path().apply {
                moveTo(0f,h*.9f); cubicTo(w*.4f,h*.63f,w*.6f,h*1.1f,w,h*.73f)
                lineTo(w,h); lineTo(0f,h); close()
            }
            drawPath(near, accent.copy(alpha = .07f))
        }
        val largeFont = androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.5f
        if (largeFont || maxWidth < 260.dp) {
            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                WeatherArtwork(data.code, data.isDay, Modifier.size(104.dp))
                WeatherTemperature(data)
            }
        } else Row(Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeatherArtwork(data.code, data.isDay, Modifier.widthIn(max = 136.dp).weight(1f).aspectRatio(1f))
            Column(Modifier.weight(1.2f)) { WeatherTemperature(data) }
        }
    }
}
@Composable
private fun WeatherTemperature(data: WeatherData) {
    Text(data.temperatureLabel, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
    Text(weatherDescription(data.code), style = MaterialTheme.typography.bodyLarge)
}
