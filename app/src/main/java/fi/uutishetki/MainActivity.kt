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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFF28594E), onPrimary = Color.White,
                background = Color(0xFFF6F5F0), surface = Color(0xFFFFFEFA),
                onBackground = Color(0xFF202C28), onSurface = Color(0xFF202C28),
                surfaceVariant = Color(0xFFE6EAE3), onSurfaceVariant = Color(0xFF4B5852)
            )) { NewsScreen() }
        }
    }
}
private val finnishDate = DateTimeFormatter.ofPattern("d.M.yyyy 'klo' HH.mm", Locale.forLanguageTag("fi-FI"))
    .withZone(ZoneId.of("Europe/Helsinki"))
private fun formatDate(millis: Long) = finnishDate.format(Instant.ofEpochMilli(millis))

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewsScreen(vm: NewsViewModel = viewModel()) {
    val news by vm.repository.news.collectAsStateWithLifecycle()
    val loading by vm.repository.loading.collectAsStateWithLifecycle()
    val refreshing by vm.repository.refreshing.collectAsStateWithLifecycle()
    val message by vm.repository.message.collectAsStateWithLifecycle()
    val updated by vm.repository.lastUpdated.collectAsStateWithLifecycle()
    val sourceName by vm.source.collectAsStateWithLifecycle()
    val topicName by vm.topic.collectAsStateWithLifecycle()
    val source = Source.entries.find { it.name == sourceName }
    val topic = Topic.entries.find { it.name == topicName }
    val visible = remember(news, source, topic) { news.filtered(source, topic) }
    val active = source != null || topic != null
    var filters by rememberSaveable { mutableStateOf(false) }
    var about by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_START) vm.refresh() }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) vm.refresh()
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(source, topic) { listState.scrollToItem(0) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("Uutishetki", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
            actions = {
                TextButton(onClick = { filters = true }) { Text(if (active) "Suodata •" else "Suodata") }
                TextButton(onClick = { about = true }) { Text("Tietoa") }
            }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
    }) { padding ->
        PullToRefreshBox(isRefreshing = refreshing, onRefresh = { vm.refresh(true) }, modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item(key = "summary") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (active) listOfNotNull(source?.label, topic?.label).joinToString(" · ") else "Pieni hetki maailman äärellä.", style = MaterialTheme.typography.titleMedium)
                        Text(if (updated > 0) "Päivitetty ${formatDate(updated)}" else "Uutiset lähteistäsi, uusimmat ensin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
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
                        if (loading || refreshing) {
                            CircularProgressIndicator()
                            Text("Haetaan uutisia…")
                        } else {
                            Text(if (active) "Näillä valinnoilla ei löytynyt uutisia." else "Ei vielä tallennettuja uutisia.", style = MaterialTheme.typography.titleMedium)
                            Text(if (active) "Kokeile toista aihetta tai lähdettä." else "Yhdistä verkkoon ja päivitä uutisvirta.")
                            if (active) TextButton(onClick = vm::clear) { Text("Tyhjennä valinnat") }
                            else TextButton(onClick = { vm.refresh(true) }) { Text("Päivitä") }
                        }
                    }
                }
                items(visible, key = { it.id }) { newsItem -> NewsCard(newsItem) }
                item(key = "footer") { Text("Otsikot ja sisällöt kuuluvat uutislähteille.\nKoko artikkeli avautuu julkaisijan sivulla.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
    if (filters) ModalBottomSheet(onDismissRequest = { filters = false }) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Suodata uutisia", style = MaterialTheme.typography.headlineSmall)
            Text("Lähde", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = source == null, onClick = { vm.source(null) }, label = { Text("Kaikki") })
                Source.entries.forEach { item -> FilterChip(selected = source == item, onClick = { vm.source(item) }, label = { Text(item.label) }) }
            }
            Text("Aihe", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = topic == null, onClick = { vm.topic(null) }, label = { Text("Kaikki") })
                Topic.entries.forEach { item -> FilterChip(selected = topic == item, onClick = { vm.topic(item) }, label = { Text(item.label) }) }
            }
            TextButton(onClick = vm::clear) { Text("Tyhjennä valinnat") }
            Button(onClick = { filters = false }, modifier = Modifier.fillMaxWidth()) { Text("Näytä uutiset") }
        }
    }
    if (about) AlertDialog(onDismissRequest = { about = false }, confirmButton = { TextButton(onClick = { about = false }) { Text("Sulje") } },
        title = { Text("Uutishetki ${BuildConfig.VERSION_NAME}") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Rauhallinen uutislukija yksityiseen käyttöön. Ei omia mainoksia, analytiikkaa tai käyttäjätilejä.")
            Text("Lähteet: Iltalehti, Ilta-Sanomat, Yle Uusimaa, Yle Häme (Kanta-Häme) ja Helsingin Sanomat. Kaikissa syötteissä ei ole kuvia tai kattavaa aiheluokitusta.")
            Text("Uutiset ja kuvat haetaan suoraan julkaisijoilta, jotka näkevät verkkopyynnön ja IP-osoitteen. Artikkelisivuilla voi olla mainoksia, seurantaa ja maksumuureja.")
            Text("Tallennetut otsikot ja kuvaukset toimivat ilman verkkoa. Kuvat ovat rajatussa välimuistissa, eikä niiden offline-saatavuutta taata. Sisällön oikeudet kuuluvat julkaisijoille.")
        } })
}
@Composable
private fun NewsCard(news: News) {
    val context = LocalContext.current
    Card(onClick = { openArticle(context, news.url) }, modifier = Modifier.fillMaxWidth(),
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
