package fi.uutishetki

import android.content.ActivityNotFoundException
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(0xFFF4F4F4.toInt(), 0xFF303030.toInt())
        )
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFF405D78), onPrimary = Color.White, surfaceTint = Color(0xFF405D78),
                primaryContainer = Color(0xFFDCE6F0), onPrimaryContainer = Color(0xFF182D40),
                secondary = Color(0xFF4D6072), onSecondary = Color.White,
                secondaryContainer = Color(0xFFDCE6F0), onSecondaryContainer = Color(0xFF182D40),
                tertiary = Color(0xFF4D6072),
                background = Color(0xFFDEDEDE), surface = Color.White,
                onBackground = Color(0xFF171717), onSurface = Color(0xFF171717),
                surfaceVariant = Color(0xFFE8E8E8), onSurfaceVariant = Color(0xFF494949),
                surfaceContainer = Color(0xFFF3F3F3), surfaceContainerLow = Color(0xFFF7F7F7),
                surfaceContainerHigh = Color(0xFFEEEEEE), surfaceContainerHighest = Color(0xFFE8E8E8),
                outline = Color(0xFF777777), outlineVariant = Color(0xFFCCCCCC)
            )) { NewsScreen() }
        }
    }
}
private val brandFont = FontFamily(Font(R.font.roboto_regular))
private val finnishDate = DateTimeFormatter.ofPattern("d.M.yyyy 'klo' HH.mm", Locale.forLanguageTag("fi-FI"))
    .withZone(ZoneId.of("Europe/Helsinki"))
internal fun formatDate(millis: Long) = finnishDate.format(Instant.ofEpochMilli(millis))

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewsScreen(vm: NewsViewModel = viewModel()) {
    val news by vm.repository.news.collectAsStateWithLifecycle()
    val loading by vm.repository.loading.collectAsStateWithLifecycle()
    val refreshing by vm.repository.refreshing.collectAsStateWithLifecycle()
    val message by vm.repository.message.collectAsStateWithLifecycle()
    val updated by vm.repository.lastUpdated.collectAsStateWithLifecycle()
    val selection by vm.selection.collectAsStateWithLifecycle()
    val source = selection.source
    val visible = remember(news, selection) { news.filtered(selection) }
    val active = source != null || selection.topics != Topic.entries.toSet()
    var about by rememberSaveable { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }
    val density = LocalDensity.current
    val safePadding = WindowInsets.safeDrawing.asPaddingValues()
    val initialHeaderHeight = safePadding.calculateTopPadding() + 56.dp
    var measuredHeaderPixels by remember { mutableIntStateOf(0) }
    val headerHeight = if (measuredHeaderPixels == 0) initialHeaderHeight else with(density) { measuredHeaderPixels.toDp() }
    val refreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_START) vm.refresh() }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) vm.refresh()
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(selection) { listState.scrollToItem(0) }
    RightFilterDrawer(drawerState = drawerState, selection = selection, updated = updated,
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
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp,
                    top = headerHeight + 8.dp, bottom = safePadding.calculateBottomPadding() + 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (message != null) item(key = "error") {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                        Column(Modifier.padding(16.dp)) {
                            Text(message.orEmpty(), color = MaterialTheme.colorScheme.onErrorContainer)
                            TextButton(onClick = { vm.refresh(true) }, enabled = !refreshing) { Text("Yritä uudelleen") }
                        }
                    }
                }
                if (visible.isEmpty()) item(key = "empty") {
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
                items(visible, key = { it.id }) { newsItem -> NewsCard(newsItem) }
                item(key = "footer") { Text("Otsikot ja sisällöt kuuluvat uutislähteille.\nKoko artikkeli avautuu julkaisijan sivulla.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        Surface(modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
            .onSizeChanged { measuredHeaderPixels = it.height },
            color = Color.White.copy(alpha = 0.94f), tonalElevation = 0.dp) {
            Column(Modifier.statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Uutishetki", modifier = Modifier.weight(1f), fontFamily = brandFont,
                        style = MaterialTheme.typography.titleLarge, color = Color.Black,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    IconButton(onClick = openDrawer, modifier = Modifier.size(48.dp)) {
                        Icon(painterResource(R.drawable.ic_menu), contentDescription = "Avaa valikko", tint = Color.Black)
                    }
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF555555))
            }
        }
    }
    }
    if (about) AlertDialog(onDismissRequest = { about = false }, confirmButton = { TextButton(onClick = { about = false }) { Text("Sulje") } },
        title = { Text("Uutishetki ${BuildConfig.VERSION_NAME}") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Rauhallinen uutislukija yksityiseen käyttöön. Ei omia mainoksia, analytiikkaa tai käyttäjätilejä.")
            Text("Nimen Roboto-kirjasin: Google, Apache License 2.0. Lisenssi sisältyy sovelluksen tiedostoihin.", style = MaterialTheme.typography.bodySmall)
            Text("Lähteet: Iltalehti, Ilta-Sanomat, Yle Uusimaa, Yle Häme (Kanta-Häme) ja Helsingin Sanomat. Kaikissa syötteissä ei ole kuvia tai kattavaa aiheluokitusta.")
            Text("Uutiset ja kuvat haetaan suoraan julkaisijoilta, jotka näkevät verkkopyynnön ja IP-osoitteen. Artikkelisivuilla voi olla mainoksia, seurantaa ja maksumuureja.")
            Text("Tallennetut otsikot ja kuvaukset toimivat ilman verkkoa. Kuvat ovat rajatussa välimuistissa, eikä niiden offline-saatavuutta taata. Sisällön oikeudet kuuluvat julkaisijoille.")
        } })
}
@Composable
private fun NewsCard(news: News) {
    val context = LocalContext.current
    Card(onClick = { openArticle(context, news.url) }, modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color(0xFFC7C7C7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        news.image?.let { image ->
            AsyncImage(model = image, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(MaterialTheme.colorScheme.surfaceVariant))
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(news.source.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(news.title, style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold)
            news.teaser?.let { Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Text(news.published?.let(::formatDate) ?: "Julkaisuaika ei saatavilla", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
private fun openArticle(context: Context, url: String) {
    val uri = Uri.parse(url)
    try { CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, uri) }
    catch (_: ActivityNotFoundException) {
        try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
        catch (_: ActivityNotFoundException) { Toast.makeText(context, "Asenna selain artikkelin avaamiseksi.", Toast.LENGTH_LONG).show() }
    }
}
