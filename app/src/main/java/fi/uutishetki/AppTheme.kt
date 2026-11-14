package fi.uutishetki

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

private val Light = lightColorScheme(
    primary = Color(0xFF405D78), onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6F0), onPrimaryContainer = Color(0xFF182D40),
    secondary = Color(0xFF4D6072), onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCE6F0), onSecondaryContainer = Color(0xFF182D40),
    tertiary = Color(0xFF4D6072), onTertiary = Color.White,
    background = Color(0xFFE0E2E5), onBackground = Color(0xFF191C20),
    surface = Color.White, onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE7EBF0), onSurfaceVariant = Color(0xFF47515C),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF5F7FA),
    surfaceContainer = Color(0xFFF0F3F7), surfaceContainerHigh = Color(0xFFE8EDF3),
    surfaceContainerHighest = Color(0xFFE1E7EE), surfaceTint = Color(0xFF405D78),
    outline = Color(0xFF707C88), outlineVariant = Color(0xFFCAD1D9),
    error = Color(0xFF962D32), onError = Color.White,
    errorContainer = Color(0xFFFBE4E4), onErrorContainer = Color(0xFF59232A)
)
private val Dark = darkColorScheme(
    primary = Color(0xFFAACDEB), onPrimary = Color(0xFF16334A),
    primaryContainer = Color(0xFF344D65), onPrimaryContainer = Color(0xFFE0EEFA),
    secondary = Color(0xFFB3C8DB), onSecondary = Color(0xFF22374A),
    secondaryContainer = Color(0xFF344D65), onSecondaryContainer = Color(0xFFE0EEFA),
    tertiary = Color(0xFFB3C8DB), onTertiary = Color(0xFF22374A),
    background = Color(0xFF171B21), onBackground = Color(0xFFF0F2F5),
    surface = Color(0xFF252E39), onSurface = Color(0xFFF0F2F5),
    surfaceVariant = Color(0xFF323E4B), onSurfaceVariant = Color(0xFFC0CAD5),
    surfaceContainerLowest = Color(0xFF14191F), surfaceContainerLow = Color(0xFF202833),
    surfaceContainer = Color(0xFF28323E), surfaceContainerHigh = Color(0xFF303C49),
    surfaceContainerHighest = Color(0xFF384655), surfaceTint = Color(0xFFAACDEB),
    outline = Color(0xFF8C9BAA), outlineVariant = Color(0xFF455363),
    error = Color(0xFFFFB4AD), onError = Color(0xFF531B20),
    errorContainer = Color(0xFF482B34), onErrorContainer = Color(0xFFFFE4E4)
)
data class AppSurfaces(val weather: Color, val weatherCard: Color)
val LocalAppSurfaces = staticCompositionLocalOf { AppSurfaces(Color(0xFFEAF2FA), Color.White) }
data class AppearanceControl(val value: Appearance, val choose: (Appearance) -> Unit)
val LocalAppearance = staticCompositionLocalOf { AppearanceControl(Appearance.SYSTEM, {}) }

@Composable
fun UutishetkiTheme(activity: ComponentActivity, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("appearance", 0) }
    var appearance by remember { mutableStateOf(Appearance.decode(prefs.getString("mode", null))) }
    val dark = appearance.isDark(isSystemInDarkTheme())
    val scheme = if (dark) Dark else Light
    SideEffect {
        activity.enableEdgeToEdge(
            statusBarStyle = if (dark) SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                else SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = if (dark) SystemBarStyle.dark(scheme.background.toArgb())
                else SystemBarStyle.light(scheme.background.toArgb(), Dark.background.toArgb())
        )
    }
    CompositionLocalProvider(
        LocalAppearance provides AppearanceControl(appearance) {
            prefs.edit().putString("mode", it.name).apply()
            appearance = it
        },
        LocalAppSurfaces provides if (dark) AppSurfaces(Color(0xFF22374D), Color(0xFF30475E))
            else AppSurfaces(Color(0xFFEAF2FA), Color(0xFFF9FCFF))
    ) {
        MaterialTheme(colorScheme = scheme) {
            Surface(color = scheme.background, contentColor = scheme.onBackground) { content() }
        }
    }
}

@Composable
fun SettingsHeading(label: String, icon: Int = R.drawable.ic_settings) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(painterResource(icon), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
@Composable
fun AppearanceSettings() {
    val control = LocalAppearance.current
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column {
        HorizontalDivider()
        TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
            Icon(painterResource(R.drawable.ic_settings), null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text("Ulkoasu")
                Text(control.value.label, style = MaterialTheme.typography.labelMedium)
            }
            Text(if (expanded) "−" else "+")
        }
        if (expanded) Appearance.entries.forEach { mode ->
            FilterChip(selected = control.value == mode, onClick = { control.choose(mode); expanded = false },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), label = { Text(mode.label) },
                leadingIcon = if (control.value == mode) { { Icon(painterResource(R.drawable.ic_check), null, Modifier.size(18.dp)) } } else null)
        }
    }
}
// Publisher resources are verified website icons, displayed without tint or circular clipping.
// Yle is intentionally text-only until the user obtains the permission required by Yle.
@Composable
fun SourceIcon(source: Source, size: Dp = 26.dp) {
    val icon = when (source) {
        Source.IL -> R.drawable.publisher_il
        Source.IS -> R.drawable.publisher_is
        Source.HS -> R.drawable.publisher_hs
        Source.STARA -> R.drawable.publisher_stara
        Source.PELAAJA -> R.drawable.publisher_pelaaja
        Source.UUSIMAA, Source.HAME -> null
    } ?: return
    Box(Modifier.size(size).background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
        .clearAndSetSemantics { }, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Image(painterResource(icon), null,
            Modifier.fillMaxSize().padding(1.dp), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
    }
}
@Composable
fun subtleSurfaceBrush() = Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface,
    MaterialTheme.colorScheme.surfaceContainerLow))
