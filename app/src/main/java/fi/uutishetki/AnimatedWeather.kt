package fi.uutishetki

import android.animation.ValueAnimator
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin

// A bounded ~30 fps clock exists only while this composition is visible and RESUMED.
// Disabled system animations keep a still illustration, without an animation loop.
@Composable
private fun weatherPhase(active: Boolean): State<Float> {
    val resolver = LocalContext.current.contentResolver
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var enabled by remember { mutableStateOf(ValueAnimator.areAnimatorsEnabled()) }
    val phase = remember { mutableFloatStateOf(0f) }
    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { enabled = ValueAnimator.areAnimatorsEnabled() }
        }
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    LaunchedEffect(active, enabled, lifecycle) {
        phase.floatValue = 0f
        if (active && enabled) lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val start = withFrameNanos { it }
            while (isActive) {
                withFrameNanos { phase.floatValue = ((it - start) % 8_000_000_000L) / 8_000_000_000f }
                delay(32)
            }
        }
    }
    return phase
}

@Composable
fun AnimatedWeatherIcon(code: Int?, day: Boolean, modifier: Modifier, animate: Boolean = true) {
    val kind = weatherMotion(code)
    val phase = weatherPhase(animate && kind != WeatherMotion.STILL)
    Box(modifier.clipToBounds()) {
        Image(painterResource(weatherArtwork(code, day)), null, Modifier.fillMaxSize().graphicsLayer {
            // Read the clock in the draw layer, not composition/layout.
            val wave = sin(phase.value * 2 * PI).toFloat()
            translationX = if (kind == WeatherMotion.CLOUD || kind == WeatherMotion.FOG) wave * size.width * .025f else 0f
            translationY = if (kind == WeatherMotion.CLEAR) wave * size.height * .015f else 0f
        })
        if (animate) WeatherParticles(kind, day, phase, Modifier.fillMaxSize(), compact = true)
    }
}

@Composable
fun WeatherAtmosphere(code: Int?, day: Boolean, modifier: Modifier) {
    val kind = weatherMotion(code)
    val phase = weatherPhase(kind != WeatherMotion.STILL)
    WeatherParticles(kind, day, phase, modifier.clipToBounds(), compact = false)
}

@Composable
private fun WeatherParticles(kind: WeatherMotion, day: Boolean, phase: State<Float>, modifier: Modifier, compact: Boolean) {
    Canvas(modifier) {
        val p = phase.value
        val w = size.width
        val h = size.height
        val alpha = if (compact) .65f else .19f
        when (kind) {
            WeatherMotion.RAIN, WeatherMotion.DRIZZLE, WeatherMotion.THUNDER, WeatherMotion.SNOW -> {
                val snow = kind == WeatherMotion.SNOW
                val drizzle = kind == WeatherMotion.DRIZZLE
                val count = if (compact) 3 else if (drizzle) 18 else 28
                repeat(count) { i ->
                    val seed = (i * .618034f) % 1f
                    val progress = (p * (if (snow) 2 else 8) + seed) % 1f
                    val x = if (compact) w * (.32f + i * .18f) else w * ((seed + if (snow) .025f * sin((p * 2 * PI + i)).toFloat() else progress * .05f + 1f) % 1f)
                    val y = if (compact) h * (.66f + progress * .24f) else progress * h
                    val fade = sin(progress * PI).toFloat()
                    val color = (if (snow) Color(0xFFE8F4FF) else Color(0xFF8BC6EC)).copy(alpha = alpha * fade)
                    if (snow || drizzle) drawCircle(color, if (compact) w * .018f else if (snow) 2.dp.toPx() else 1.dp.toPx(), Offset(x, y))
                    else drawLine(color, Offset(x, y), Offset(x - w * .012f, y + if (compact) h * .07f else 10.dp.toPx()),
                        strokeWidth = if (compact) w * .015f else 1.dp.toPx(), cap = StrokeCap.Round)
                }
            }
            WeatherMotion.CLOUD, WeatherMotion.FOG -> if (!compact) {
                repeat(3) { i ->
                    val drift = sin(p * 2 * PI + i).toFloat() * w * .06f
                    drawOval(Color(0xFFC2D5E6).copy(alpha = if (kind == WeatherMotion.FOG) .06f else .035f),
                        topLeft = Offset(-w * .2f + drift, h * (.26f + i * .18f)), size = Size(w * 1.3f, h * .12f))
                }
            }
            WeatherMotion.CLEAR -> if (!compact) {
                val wave = sin(p * 2 * PI).toFloat()
                drawCircle((if (day) Color(0xFFFFE8AF) else Color(0xFFDBE8FA)).copy(alpha = .035f),
                    radius = w * (.16f + wave * .006f), center = Offset(w * .24f, h * .3f))
            }
            WeatherMotion.STILL -> Unit
        }
    }
}
