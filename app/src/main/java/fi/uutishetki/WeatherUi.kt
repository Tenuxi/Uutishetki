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
private fun WeatherIcon(symbol: WeatherSymbol, modifier: Modifier = Modifier.size(18.dp)) {
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
    Icon(painterResource(resource), contentDescription = null, modifier = modifier)
}
@Composable
fun WeatherDetails(state: WeatherUiState, onSettings: () -> Unit, onDismiss: () -> Unit) {
    var informationOpen by rememberSaveable { mutableStateOf(false) }
    val data = state.data
    val context = LocalContext.current
    val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp),
            contentAlignment = Alignment.Center) {
            val availableHeight = maxHeight
            Surface(Modifier.widthIn(max = 560.dp).fillMaxWidth().heightIn(max = availableHeight),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                color = androidx.compose.ui.graphics.Color(0xFFF0F4F8), tonalElevation = 0.dp) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text((data?.place ?: state.requestedPlace).name, style = MaterialTheme.typography.titleLarge)
                            Text((data?.origin ?: state.origin).label, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = onSettings) {
                            Icon(painterResource(R.drawable.ic_settings), "Sääasetukset")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(painterResource(R.drawable.ic_close), "Sulje säätiedot")
                        }
                    }
                    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (state.loading) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                            Text("Päivitetään säätä…", style = MaterialTheme.typography.bodySmall)
                        }
                        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        if (data != null) {
                            if (!data.isFresh(state.now) || state.error != null) {
                                Text("Aiempi säätieto ja ennuste – ajantasaisuutta ei voitu varmistaa.", color = MaterialTheme.colorScheme.error)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                WeatherIcon(weatherSymbol(data.code, data.isDay), Modifier.size(56.dp))
                                Column {
                                    Text(data.temperatureLabel, style = MaterialTheme.typography.displayMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    Text(weatherDescription(data.code), style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        } else if (!state.loading) Text("Säätieto ei saatavilla.")
                        BoxWithConstraints(Modifier.fillMaxWidth()) {
                            val dates = forecastDates(state.now)
                            if (maxWidth >= 360.dp && fontScale <= 1.3f) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    dates.forEachIndexed { index, date ->
                                        ForecastCard(index, date, data?.forecast?.find { it.date == date }, state.loading, Modifier.weight(1f))
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    dates.forEachIndexed { index, date ->
                                        ForecastCard(index, date, data?.forecast?.find { it.date == date }, state.loading, Modifier.fillMaxWidth())
                                    }
                                }
                            }
                        }
                        data?.let { Text(weatherUpdatedLabel(it.observedAt, state.now), style = MaterialTheme.typography.bodySmall) }
                        TextButton(onClick = { informationOpen = !informationOpen }, modifier = Modifier.semantics {
                            contentDescription = "Tietoa säästä, ${if (informationOpen) "sulje lisätiedot" else "avaa lisätiedot"}"
                        }) { Text("Tietoa säästä ${if (informationOpen) "−" else "+"}") }
                        if (informationOpen) {
                            Text("Sää on mallipohjainen arvio, ei paikallisen mittausaseman havainto. Lämpötilat on pyöristetty kokonaisasteiksi. Sade enint. tarkoittaa päivän suurinta tuntikohtaista sateen todennäköisyyttä.", style = MaterialTheme.typography.bodySmall)
                            data?.let {
                                Text("Säätiedon ajankohta ${formatDate(it.observedAt)}\nHaettu ${formatDate(it.fetchedAt)}\nAjat: Europe/Helsinki.", style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = { openArticle(context, "https://www.geonames.org/") }) { Text("Paikkakunnat: GeoNames", style = MaterialTheme.typography.labelMedium) }
                        }
                        HorizontalDivider()
                        WeatherPanelAttribution()
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastCard(index: Int, date: java.time.LocalDate, forecast: DailyForecast?, loading: Boolean, modifier: Modifier) {
    Surface(modifier, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), color = androidx.compose.ui.graphics.Color.White) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (index == 0) "Huomenna" else "Ylihuomenna", style = MaterialTheme.typography.titleSmall)
            Text(date.format(java.time.format.DateTimeFormatter.ofPattern("d.M.")), style = MaterialTheme.typography.bodySmall)
            if (forecast == null) Text(if (loading) "Haetaan ennustetta…" else "Ennuste ei saatavilla.")
            else {
                WeatherIcon(forecast.code?.let { weatherSymbol(it, true) } ?: WeatherSymbol.UNKNOWN, Modifier.size(32.dp))
                Text(forecast.code?.let(::weatherDescription) ?: "Säätila ei saatavilla", style = MaterialTheme.typography.bodyMedium)
                Text("Ylin ${weatherDegrees(forecast.high)}", style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text("Alin ${weatherDegrees(forecast.low)}", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_drop), null, Modifier.size(16.dp))
                    Text(forecast.rain?.let { "Sade enint. $it %" } ?: "Sadetieto puuttuu", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeatherPanelAttribution() {
    val context = LocalContext.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(onClick = { openArticle(context, "https://open-meteo.com/") }, contentPadding = PaddingValues(horizontal = 4.dp)) {
            Text("Sää: Open-Meteo", style = MaterialTheme.typography.labelMedium)
        }
        TextButton(onClick = { openArticle(context, "https://creativecommons.org/licenses/by/4.0/") }, contentPadding = PaddingValues(horizontal = 4.dp)) {
            Text("CC BY 4.0", style = MaterialTheme.typography.labelMedium)
        }
    }
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
