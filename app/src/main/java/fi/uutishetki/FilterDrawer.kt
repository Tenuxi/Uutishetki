package fi.uutishetki

import androidx.compose.foundation.layout.*
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
                            drawerContainerColor = Color.White,
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
                            LazyColumn(Modifier.weight(1f).fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        GroupTitle("Lehdet")
                                        SelectionChip("Kaikki lehdet", selection.source == null, { onSource(null) }, Modifier.fillMaxWidth())
                                        Source.entries.forEach { source ->
                                            SelectionChip(source.label, selection.source == source, { onSource(source) }, Modifier.fillMaxWidth())
                                        }
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
                                        TextButton(onClick = onAllTopics, modifier = Modifier.heightIn(min = 48.dp)) { Text("Valitse kaikki") }
                                        Text("Luokittelemattomat näkyvät oletuksilla ja kaikkien aiheiden valinnalla.",
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                item {
                                    OutlinedButton(onClick = onDefaults, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                                        Text("Palauta oletukset")
                                    }
                                }
                                item { WeatherSettingsContent(weather, onAutomaticWeather) }
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
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(16.dp),
        label = { Text(label) },
        leadingIcon = if (selected) { {
            Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(16.dp))
        } } else null,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFFFAFAFA),
            selectedContainerColor = Color(0xFFDCE6F0),
            selectedLabelColor = Color(0xFF182D40),
            selectedLeadingIconColor = Color(0xFF405D78)
        ),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected,
            borderColor = Color(0xFFD1D5D9), selectedBorderColor = Color(0xFFB9C9D8))
    )
}
