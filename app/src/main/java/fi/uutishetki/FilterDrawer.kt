package fi.uutishetki

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.selected
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RightFilterDrawer(
    weather: WeatherViewModel,
    traffic: TrafficViewModel,
    onAutomaticWeather: () -> Unit,
    drawerState: DrawerState,
    selection: Selection,
    updated: Long,
    onSource: (Source?) -> Unit,
    onTopic: (Topic) -> Unit,
    onAllTopics: () -> Unit,
    onDefaults: () -> Unit,
    onAbout: () -> Unit,
    content: @Composable () -> Unit
) {
    val contentDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    var helpOpen by rememberSaveable { mutableStateOf(false) }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Material's modal drawer supplies scrim dismissal, back handling.
        // RTL positions only the drawer mechanics on the right; contents retain their direction.
        val panelShape = AbsoluteRoundedCornerShape(topLeft = 24.dp, bottomLeft = 24.dp)
        val panelWidth = (maxWidth - 40.dp).coerceAtMost(360.dp).coerceAtLeast(0.dp)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = drawerState.isOpen,
                scrimColor = Color.Black.copy(alpha = 0.36f),
                drawerContent = {
                        ModalDrawerSheet(
                            drawerState = drawerState,
                            modifier = Modifier.width(panelWidth).fillMaxHeight().shadow(12.dp, panelShape, clip = false),
                            drawerShape = panelShape,
                            drawerContainerColor = MaterialTheme.colorScheme.surface,
                            drawerContentColor = MaterialTheme.colorScheme.onSurface,
                            drawerTonalElevation = 0.dp,
                            windowInsets = WindowInsets.safeDrawing
                        ) {
                    CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("Oma uutishetki", modifier = Modifier.weight(1f).semantics { heading() },
                                    style = MaterialTheme.typography.titleLarge)
                                IconButton(onClick = { scope.launch { drawerState.close() } }, modifier = Modifier.size(48.dp)) {
                                    Icon(painterResource(R.drawable.ic_close), contentDescription = "Sulje valikko")
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            LazyColumn(Modifier.weight(1f).fillMaxWidth().clipToBounds(), state = scrollState,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        GroupTitle("Lehdet")
                                        SelectionChip("Kaikki lehdet", selection.source == null, { onSource(null) }, Modifier.fillMaxWidth())
                                        SourceChoices(selection.source, onSource)

                                    }
                                }
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        GroupTitle("Aiheet")
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Topic.entries.forEach { topic ->
                                                SelectionChip(topic.label, topic in selection.topics, { onTopic(topic) })
                                            }
                                        }
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            TextButton(onClick = onAllTopics) { Text("Valitse kaikki") }
                                            TextButton(onClick = onDefaults) { Text("Palauta oletukset") }
                                        }
                                        TextButton(onClick = { helpOpen = !helpOpen }) { Text("Tietoa aihevalinnoista") }
                                        if (helpOpen) Text("Luokittelemattomat näkyvät oletuksilla ja kaikkien aiheiden valinnalla.",
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                item { AppearanceSettings() }
                                item { WeatherSettingsContent(weather, onAutomaticWeather) }
                                item { TrafficSettingsContent(traffic, weather) }
                                // Footer shares the scroll container so it cannot overlap the choices.
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                        TextButton(onClick = onAbout, modifier = Modifier.heightIn(min = 48.dp)) { Text("Tietoa") }
                                        Text("Uutishetki ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(if (updated > 0) "Päivitetty ${formatDate(updated)}" else "Uutisia ei vielä päivitetty",
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides contentDirection) {
                    Box(if (drawerState.isOpen) Modifier.clearAndSetSemantics { } else Modifier) { content() }
                }
            }
        }
    }
}

@Composable
private fun GroupTitle(label: String) {
    Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
}

@Composable
private fun SelectionChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(selected = selected, onClick = onClick, modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp), label = { Text(label) },
        leadingIcon = if (selected) { { Icon(painterResource(R.drawable.ic_check), null, Modifier.size(16.dp)) } } else null,
        colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant, selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = .6f)))
}

@Composable
private fun SourceChoices(selected: Source?, onSource: (Source) -> Unit) {
    val measurer = rememberTextMeasurer()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val style = MaterialTheme.typography.labelLarge
    // Measure full short names and the longest word of HS at the actual user's font scale.
    val textWidth = Source.entries.maxOf { source ->
        val parts = if (source == Source.HS) source.label.split(' ') else listOf(source.label)
        parts.maxOf { measurer.measure(it, style = style, softWrap = false).size.width }
    }
    BoxWithConstraints {
        val required = with(density) { textWidth.toDp() } + 30.dp + 8.dp + 16.dp
        val columns = if (maxWidth >= required * 2 + 8.dp && density.fontScale <= 1.3f) 2 else 1
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Source.entries.chunked(columns).forEach { sources ->
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    sources.forEach { source ->
                        val active = selected == source
                        Surface(onClick = { onSource(source) },
                            modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 56.dp).semantics { this.selected = active },
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            border = androidx.compose.foundation.BorderStroke(.7.dp,
                                if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                            Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SourceIcon(source, 30.dp)
                                Text(source.label, Modifier.weight(1f), style = style)
                            }
                        }
                    }
                    if (sources.size < columns) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
