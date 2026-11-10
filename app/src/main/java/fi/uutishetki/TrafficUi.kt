package fi.uutishetki

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

private fun distanceLabel(distance: Double) = if (distance < 1) "alle 1 km geometriasta" else "noin ${distance.roundToInt()} km geometriasta"

@Composable
fun TrafficCard(item: NearbyTraffic, checkedAt: Long, onOpen: () -> Unit = {}) {
    var details by remember { mutableStateOf(false) }
    val notice = item.notice
    Card(onClick = { onOpen(); details = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD6), contentColor = Color(0xFF292323)),
        border = BorderStroke(1.dp, Color(0xFFD7ABA6)), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource(R.drawable.ic_traffic), contentDescription = null, modifier = Modifier.size(20.dp))
                Text("Liikennetiedote", style = MaterialTheme.typography.labelLarge)
            }
            Text(notice.title, style = MaterialTheme.typography.titleMedium)
            notice.location?.let { Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis) }
            if (notice.description.isNotBlank()) Text(notice.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Text("Julkaistu ${formatDate(notice.published)}", style = MaterialTheme.typography.bodySmall)
            if (notice.updated != notice.published) Text("Päivitetty ${formatDate(notice.updated)}", style = MaterialTheme.typography.bodySmall)
            Text("${distanceLabel(item.distance)} · Fintraffic / Digitraffic", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (details) {
        val context = LocalContext.current
        AlertDialog(onDismissRequest = { details = false }, title = { Text("Liikennetiedote") },
            confirmButton = { TextButton(onClick = { details = false }) { Text("Sulje") } },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(notice.title, style = MaterialTheme.typography.titleMedium)
                    notice.location?.let { Text(it) }
                    Text(notice.description)
                    Text("Julkaistu ${formatDate(notice.published)}\nPäivitetty ${formatDate(notice.updated)}\nVersio ${notice.version}")
                    Text("Alkanut ${formatDate(notice.start)}" + (notice.end?.let { "\nPäättyy ${formatDate(it)}" } ?: "\nPäättymisaikaa ei ilmoitettu."))
                    Text("${distanceLabel(item.distance)}. Etäisyys lähimpään lähteen ilmoittaman geometrian kohtaan; sijainnin tarkkuus vaikuttaa arvioon.")
                    Text("Tarkistettu ${formatDate(checkedAt)}. Ei kattava tai takuuvarmasti reaaliaikainen hälytyspalvelu.")
                    notice.sender?.let { Text(it) }
                    notice.sourceLink?.let { link -> TextButton(onClick = { openArticle(context, link) }) { Text("Lähteen lisätietolinkki") } }
                    TrafficAttribution()
                }
            })
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrafficSettingsContent(vm: TrafficViewModel, weather: WeatherViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val state by vm.state.collectAsStateWithLifecycle()
    val locationSettings by weather.settings.collectAsStateWithLifecycle()
    val located by weather.currentLocation.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        Text("Lähialueen liikennetiedotteet", style = MaterialTheme.typography.titleMedium)
        Row {
            Text("Näytä liikennetiedotteet", Modifier.weight(1f))
            Switch(settings.enabled, vm::enabled, modifier = Modifier.semantics { contentDescription = "Näytä lähialueen liikennetiedotteet" })
        }
        if (settings.enabled) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TRAFFIC_RADII.forEach { radius -> FilterChip(selected = settings.radius == radius, onClick = { vm.radius(radius) }, label = { Text("$radius km") }) }
            }
            locationSettings.trafficPlace(located)?.let {
                Text("Sijainti: ${it.name}")
                Text(if (locationSettings.automatic) "Etäisyys arvioidaan laitteen likimääräisestä sijainnista." else "Etäisyys lasketaan valitun paikkakunnan keskipisteestä.", style = MaterialTheme.typography.bodySmall)
            }
            Text("Vaihda sijaintia yllä paikkakunnan valinnasta tai automaattisen sijainnin asetuksesta.", style = MaterialTheme.typography.bodySmall)
            state.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            if (state.checkedAt > 0) Text("Viimeisin onnistunut haku ${formatDate(state.checkedAt)}", style = MaterialTheme.typography.bodySmall)
        }
        Text("Liikenteen häiriötiedotteet, ei kaikkien pelastustehtävien hälytyslista. Ei taustaseurantaa tai ilmoituksia.", style = MaterialTheme.typography.bodySmall)
        TrafficAttribution()
    }
}
@Composable
private fun TrafficAttribution() {
    val context = LocalContext.current
    TextButton(onClick = { openArticle(context, "https://www.digitraffic.fi/tieliikenne/") }) { Text("Fintraffic / Digitraffic") }
    TextButton(onClick = { openArticle(context, "https://creativecommons.org/licenses/by/4.0/") }) { Text("CC BY 4.0 · Lähteen teksti, sovelluksen etäisyysarvio") }
}
