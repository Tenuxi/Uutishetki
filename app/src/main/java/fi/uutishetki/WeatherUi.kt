package fi.uutishetki

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun WeatherButton(state: WeatherUiState, onClick: () -> Unit) {
    val current = state.current
    TextButton(onClick = onClick, modifier = Modifier.widthIn(max = 156.dp).heightIn(min = 48.dp)
        .semantics { contentDescription = if (current != null) "${current.place.name}, sää: ${current.temperatureLabel}, ${weatherDescription(current.code)}. Open-Meteo. Avaa säätiedot."
            else "${state.requestedPlace.name}: ${if (state.loading) "Säätä haetaan" else "Sää ei saatavilla"}. Avaa säätiedot." }, contentPadding = PaddingValues(horizontal = 6.dp)) {
        WeatherIcon(current?.let { weatherSymbol(it.code, it.isDay) } ?: WeatherSymbol.UNKNOWN)
        Spacer(Modifier.width(4.dp))
        Column(Modifier.weight(1f, fill = false)) {
            Text(current?.temperatureLabel ?: if (state.loading) "…" else "—", fontSize = 14.sp, maxLines = 1)
            Text((current?.place ?: state.requestedPlace).name.substringBefore(","), fontSize = 11.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
@Composable
private fun WeatherIcon(symbol: WeatherSymbol) {
    val resource = when (symbol) {
        WeatherSymbol.SUN -> R.drawable.weather_sun
        WeatherSymbol.MOON -> R.drawable.weather_moon
        WeatherSymbol.CLOUD -> R.drawable.weather_cloud
        WeatherSymbol.RAIN -> R.drawable.weather_rain
        WeatherSymbol.SNOW -> R.drawable.weather_snow
        WeatherSymbol.THUNDER -> R.drawable.weather_thunder
        WeatherSymbol.FOG -> R.drawable.weather_fog
        WeatherSymbol.UNKNOWN -> R.drawable.weather_unknown
    }
    Icon(painterResource(resource), contentDescription = null, modifier = Modifier.size(18.dp))
}
@Composable
fun WeatherDetails(state: WeatherUiState, automatic: Boolean, onSettings: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Paikallinen sää") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Sulje") } },
        dismissButton = { TextButton(onClick = onSettings) { Text("Sääasetukset") } },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state.loading) Text("Päivitetään säätä…")
            state.error?.let { Text(it) }
                val data = state.data
                if (data != null) {
                    Text(data.place.name, style = MaterialTheme.typography.titleMedium)
                    if (!data.isFresh(state.now) || state.error != null) {
                        Text("Aiempi säätieto – ei ajantasainen tieto nykyisestä säästä.", color = MaterialTheme.colorScheme.error)
                    }
                    Text("${data.temperatureLabel} · ${weatherDescription(data.code)}", style = MaterialTheme.typography.titleLarge)
                    Text("Säätiedon ajankohta ${formatDate(data.observedAt)}")
                    Text("Haettu ${formatDate(data.fetchedAt)}")
                } else if (!state.loading && state.error == null) Text("Säätietoa ei vielä ole. Valitse sijainti sääasetuksista.")
                Text(if (automatic) "Sää kohdistetaan saatavilla olevaan sijaintiin tai yllä nimettyyn varapaikkakuntaan." else "Sää kohdistetaan valitun paikkakunnan keskipisteeseen.")
                Text("Sää on mallipohjainen arvio, ei paikallinen mittaus. Lämpötila on pyöristetty kokonaisasteiksi.")
                WeatherAttribution()
            }
        })
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeatherSettingsContent(vm: WeatherViewModel, onAutomatic: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val state by vm.state.collectAsStateWithLifecycle()
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    val locationMessage by vm.locationMessage.collectAsStateWithLifecycle()
    val located by vm.currentLocation.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        Text("Paikallinen sää", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Näytä sää yläpalkissa", Modifier.weight(1f))
            Switch(checked = settings.enabled, onCheckedChange = vm::enabled,
                modifier = Modifier.semantics { contentDescription = "Näytä sää yläpalkissa" })
        }
        Text("Vapaaehtoinen. Open-Meteo saa valitun paikan tai pyöristetyt koordinaatit ja IP-osoitteen. Ei taustapaikannusta.",
            style = MaterialTheme.typography.bodySmall)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sään ja liikennetiedotteiden sijainti", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = settings.automatic, onClick = onAutomatic, label = { Text("Automaattinen sijainti") })
                FilterChip(selected = !settings.automatic, onClick = { vm.manualMode() }, label = { Text("Paikkakunta käsin") })
            }
            if (settings.automatic) {
                Text(located?.name ?: "Sään varapaikka: ${settings.weatherPlace(null).name}", style = MaterialTheme.typography.bodyMedium)
                Text("Likimääräinen sijainti päivitetään vain sovellusta käytettäessä. Paikannimen haku käyttää laitteen paikkapalvelua.", style = MaterialTheme.typography.bodySmall)
            } else {
                Text(settings.manual?.name ?: "Paikkakuntaa ei valittu")
                TextButton(onClick = { searchOpen = true }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Valitse paikkakunta") }
                Text("Sää ja liikennetiedotteiden etäisyydet käyttävät paikkakunnan keskipistettä, eivät omaa tarkkaa sijaintiasi.", style = MaterialTheme.typography.bodySmall)
            }
            locationMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            state.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            WeatherAttribution()
        }

    }
    if (searchOpen) PlaceSearchDialog(vm, onDismiss = { searchOpen = false; vm.dismissSearch() })
}
@Composable
private fun PlaceSearchDialog(vm: WeatherViewModel, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val search by vm.search.collectAsStateWithLifecycle()
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Valitse paikkakunta") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Sulje") } },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = query, onValueChange = { query = it.take(80); vm.dismissSearch() },
                    label = { Text("Paikkakunta Suomessa") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = { vm.searchPlaces(query) }, enabled = !search.busy && query.trim().length >= 3) { Text("Hae") }
                if (search.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                search.message?.let { Text(it) }
                search.results.forEach { place ->
                    OutlinedButton(onClick = { vm.choose(place); onDismiss() }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(place.name) }
                }
                Text("Paikkakuntahaku: Open-Meteo / GeoNames. Haku lähetetään vain Hae-painikkeesta.", style = MaterialTheme.typography.bodySmall)
                WeatherAttribution()
            }
        })
}
@Composable
private fun WeatherAttribution() {
    val context = LocalContext.current
    Column {
        TextButton(onClick = { openArticle(context, "https://open-meteo.com/") }) { Text("Sää: Open-Meteo") }
        TextButton(onClick = { openArticle(context, "https://creativecommons.org/licenses/by/4.0/") }) { Text("Säätiedon lisenssi: CC BY 4.0") }
        TextButton(onClick = { openArticle(context, "https://www.geonames.org/") }) { Text("Paikkakunnat: GeoNames") }
    }
}
