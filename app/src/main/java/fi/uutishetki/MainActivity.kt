package fi.uutishetki

import android.Manifest
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val newsModel by lazy { androidx.lifecycle.ViewModelProvider(this)[NewsViewModel::class.java] }
    override fun onStart() { super.onStart(); newsModel.foreground() }
    override fun onStop() {
        if (!isChangingConfigurations) newsModel.opening.stopped()
        super.onStop()
    }
    fun externalLaunch() { newsModel.opening.externalLaunch() }
    fun externalFailed() { newsModel.opening.externalFailed() }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("externalReturn", newsModel.opening.externalPending)
        super.onSaveInstanceState(outState)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.getBoolean("externalReturn") == true) newsModel.opening.restoreExternalReturn()
        setContent { UutishetkiTheme(this) { NewsScreen(vm = newsModel) } }
    }
}
private val brandFont = FontFamily(Font(R.font.roboto_regular))
private val finnishDate = DateTimeFormatter.ofPattern("d.M.yyyy 'klo' HH.mm", Locale.forLanguageTag("fi-FI"))
    .withZone(ZoneId.of("Europe/Helsinki"))
internal fun formatDate(millis: Long) = finnishDate.format(Instant.ofEpochMilli(millis))

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewsScreen(vm: NewsViewModel = viewModel(), weather: WeatherViewModel = viewModel(), traffic: TrafficViewModel = viewModel()) {
    val trafficState by traffic.state.collectAsStateWithLifecycle()
    val trafficSettings by traffic.settings.collectAsStateWithLifecycle()
    val offerPermission by weather.offerPermission.collectAsStateWithLifecycle()
    val located by weather.currentLocation.collectAsStateWithLifecycle()
    val weatherSettings by weather.settings.collectAsStateWithLifecycle()
    val weatherState by weather.state.collectAsStateWithLifecycle()
    var weatherDetails by rememberSaveable { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) weather.automatic() else weather.permissionDenied()
    }
    val automaticWeather: () -> Unit = {
        weather.automatic()
        if (!weather.hasLocationPermission()) permission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    val news by vm.repository.news.collectAsStateWithLifecycle()
    val loading by vm.repository.loading.collectAsStateWithLifecycle()
    val refreshing by vm.repository.refreshing.collectAsStateWithLifecycle()
    val message by vm.repository.message.collectAsStateWithLifecycle()
    val updated by vm.repository.lastUpdated.collectAsStateWithLifecycle()
    val selection by vm.selection.collectAsStateWithLifecycle()
    val source = selection.source
    val visible = remember(news, selection) { news.filtered(selection) }
    val nearby = if (trafficSettings.enabled && weatherSettings.trafficPlace(located) != null && trafficState.place == weatherSettings.trafficPlace(located) && trafficState.radius == trafficSettings.radius) trafficState.nearby else emptyList()
    val feed = remember(visible, nearby) { mergeFeed(visible, nearby) }
    var today by remember { mutableStateOf(helsinkiDay(System.currentTimeMillis())) }
    val dayGroups = remember(feed) { groupFeedDays(feed) }
    val openingCompleted by vm.openingCompleted.collectAsStateWithLifecycle()
    val active = source != null || selection.topics != Topic.entries.toSet()
    var about by rememberSaveable { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = { vm.opening.userInteracted(); scope.launch { drawerState.open() } }
    val density = LocalDensity.current
    val safePadding = WindowInsets.safeDrawing.asPaddingValues()
    val initialHeaderHeight = safePadding.calculateTopPadding() + 56.dp
    var measuredHeaderPixels by remember { mutableIntStateOf(0) }
    val headerHeight = if (measuredHeaderPixels == 0) initialHeaderHeight else with(density) { measuredHeaderPixels.toDp() }
    val refreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                today = helsinkiDay(System.currentTimeMillis())
                val next = today.plusDays(1).atStartOfDay(HELSINKI_ZONE).toInstant().toEpochMilli()
                delay((next - System.currentTimeMillis()).coerceIn(1, 60_000))
            }
        }
    }
    LaunchedEffect(openingCompleted) {
        if (openingCompleted > 0) {
            snapshotFlow { news == vm.repository.news.value }.first { it }
            withFrameNanos { }
            if (vm.opening.consumeScroll(openingCompleted)) listState.scrollToItem(0)
        }
    }
    val readingScroll = remember(vm) {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (source == androidx.compose.ui.input.nestedscroll.NestedScrollSource.UserInput) vm.opening.userInteracted()
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }
    LaunchedEffect(lifecycle, weather) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            combine(weather.settings, weather.refreshToken, weather.currentLocation) { settings, token, place -> Triple(settings, token, place) }.collectLatest {
                while (isActive) { weather.refresh(); delay(60_000) }
            }
        }
    }
    LaunchedEffect(lifecycle, weather, traffic) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try {
                combine(weather.settings, weather.refreshToken, traffic.settings) { settings, token, trafficConfig -> Triple(settings, token, trafficConfig.enabled) }.collectLatest {
                    while (isActive) {
                        if (it.first.enabled || it.third) weather.refreshLocation() else weather.onBackground()
                        delay(5 * 60_000L)
                    }
                }
            } finally { weather.onBackground() }
        }
    }
    LaunchedEffect(lifecycle, traffic, weather) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            combine(weather.settings, weather.currentLocation, traffic.settings) { config, place, _ -> config.trafficPlace(place) }.collectLatest { place ->
                while (isActive) { traffic.refresh(place); delay(15_000) }
            }
        }
    }
    var previousSelection by remember { mutableStateOf(selection) }
    LaunchedEffect(selection) {
        if (selection != previousSelection) { vm.opening.userInteracted(); listState.scrollToItem(0); previousSelection = selection }
    }
    RightFilterDrawer(weather = weather, traffic = traffic, onAutomaticWeather = automaticWeather, drawerState = drawerState, selection = selection, updated = updated,
        onSource = vm::source, onTopic = vm::toggle, onAllTopics = vm::allTopics,
        onDefaults = vm::defaults, onAbout = { scope.launch { drawerState.close(); about = true } }) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
        // Content padding scrolls with the list; the fixed translucent header stays above it.
        PullToRefreshBox(isRefreshing = refreshing, onRefresh = { vm.refresh(true) },
            state = refreshState, modifier = Modifier.fillMaxSize(), indicator = {
                PullToRefreshDefaults.Indicator(state = refreshState, isRefreshing = refreshing,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = headerHeight))
            }) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().nestedScroll(readingScroll),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp,
                    top = headerHeight + 8.dp, bottom = safePadding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (trafficSettings.enabled && trafficState.unavailable) item(key = "traffic-error") {
                    Text(trafficState.message.orEmpty(), style = MaterialTheme.typography.bodySmall)
                }
                if (message != null) item(key = "error") {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                        Column(Modifier.padding(16.dp)) {
                            Text(message.orEmpty(), color = MaterialTheme.colorScheme.onErrorContainer)
                            TextButton(onClick = { vm.refresh(true) }, enabled = !refreshing) { Text("Yritä uudelleen") }
                        }
                    }
                }
                if (feed.isEmpty()) item(key = "empty") {
                    Column(Modifier.fillMaxWidth().padding(vertical = 48.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (selection.topics.isEmpty()) {
                            Text("Ei aiheita valittu", style = MaterialTheme.typography.titleMedium)
                            Text("Valitse vähintään yksi aihe valikosta.")
                            TextButton(onClick = openDrawer) { Text("Valitse aiheet") }
                        } else if (loading || refreshing) {
                            CircularProgressIndicator()
                            Text("Haetaan uutisia…")
                        } else {
                            Text(if (active) "Näillä valinnoilla ei löytynyt uutisia." else "Ei vielä tallennettuja uutisia.", style = MaterialTheme.typography.titleMedium)
                            Text(if (active) "Kokeile toista aihetta tai lähdettä." else "Yhdistä verkkoon ja päivitä uutisvirta.")
                            if (active) TextButton(onClick = vm::defaults) { Text("Palauta oletukset") }
                            else TextButton(onClick = { vm.refresh(true) }) { Text("Päivitä") }
                        }
                    }
                }
                dayGroups.forEach { group ->
                    item(key = "day:${group.date}") {
                        Column(Modifier.fillMaxWidth().padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(dayLabel(group.date, today), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                    items(group.entries, key = { it.key }) { entry ->
                        when (entry) {
                            is FeedEntry.Article -> NewsCard(entry.news)
                            is FeedEntry.Traffic -> TrafficCard(entry.item, trafficState.checkedAt, onOpen = { vm.opening.userInteracted() })
                        }
                    }
                }
                item(key = "footer") { Text("Otsikot ja sisällöt kuuluvat uutislähteille.\nKoko artikkeli avautuu julkaisijan sivulla.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        Surface(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
            .onSizeChanged { measuredHeaderPixels = it.height },
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f), tonalElevation = 0.dp) {
            Column(Modifier.statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Uutishetki", modifier = Modifier.weight(1f), fontFamily = brandFont,
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (weatherSettings.enabled) WeatherButton(weatherState) { vm.opening.userInteracted(); weatherDetails = true }
                    IconButton(onClick = openDrawer, modifier = Modifier.size(48.dp)) {
                        Icon(painterResource(R.drawable.ic_menu), contentDescription = "Avaa valikko", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
    }
    if (offerPermission) AlertDialog(onDismissRequest = { weather.permissionExplained() },
        title = { Text("Paikallinen sää ja liikennetiedotteet") },
        text = { Text("Sijaintia käytetään paikalliseen säähän ja lähialueen liikennetiedotteisiin vain sovellusta käyttäessäsi. Sijaintihistoriaa ei tallenneta. Ilman lupaa näytämme Helsingin sään; voit myös valita paikkakunnan käsin. Open-Meteo saa sään koordinaatit ja IP-osoitteen.") },
        confirmButton = { TextButton(onClick = { weather.permissionExplained(); automaticWeather() }) { Text("Jatka sijaintilupaan") } },
        dismissButton = { TextButton(onClick = { weather.permissionExplained() }) { Text("Ei nyt") } })
    if (weatherDetails) WeatherDetails(weatherState,
        onSettings = { weatherDetails = false; openDrawer() }, onDismiss = { weatherDetails = false })
    if (about) AlertDialog(onDismissRequest = { about = false }, confirmButton = { TextButton(onClick = { about = false }) { Text("Sulje") } },
        title = { Text("Uutishetki ${BuildConfig.VERSION_NAME}") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Rauhallinen uutislukija yksityiseen käyttöön. Ei omia mainoksia, analytiikkaa tai käyttäjätilejä.")
            Text("Nimen Roboto-kirjasin: Google, Apache License 2.0. Lisenssi sisältyy sovelluksen tiedostoihin.", style = MaterialTheme.typography.bodySmall)
            Text("Lähteet: Iltalehti, Ilta-Sanomat, Yle Uusimaa, Yle Häme (Kanta-Häme), Helsingin Sanomat, Stara ja Pelaaja. Kaikissa syötteissä ei ole kuvia tai kattavaa aiheluokitusta.")
            Text("Uutiset ja kuvat haetaan suoraan julkaisijoilta, jotka näkevät verkkopyynnön ja IP-osoitteen. Artikkelisivuilla voi olla mainoksia, seurantaa ja maksumuureja.")
            Text("Tallennetut otsikot ja kuvaukset toimivat ilman verkkoa. Kuvat ovat rajatussa välimuistissa, eikä niiden offline-saatavuutta taata. Sisällön oikeudet kuuluvat julkaisijoille.")
        } })
}
@Composable
private fun NewsCard(news: News) {
    val context = LocalContext.current
    Card(onClick = { openArticle(context, news.url) }, modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        news.image?.let { image ->
            AsyncImage(model = image, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(MaterialTheme.colorScheme.surfaceVariant))
        }
        Column(Modifier.background(subtleSurfaceBrush()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SourceBadge(news.source)
                Text(news.source.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(news.title, style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
            news.teaser?.let { Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(news.published?.let(::formatDate) ?: "Julkaisuaika ei saatavilla", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
internal fun openArticle(context: Context, url: String) {
    var base = context
    while (base is android.content.ContextWrapper && base !is MainActivity) base = base.baseContext
    val activity = base as? MainActivity
    activity?.externalLaunch()
    val uri = Uri.parse(url)
    try { CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, uri) }
    catch (_: ActivityNotFoundException) {
        try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
        catch (_: ActivityNotFoundException) { activity?.externalFailed(); Toast.makeText(context, "Asenna selain artikkelin avaamiseksi.", Toast.LENGTH_LONG).show() }
    }
}
