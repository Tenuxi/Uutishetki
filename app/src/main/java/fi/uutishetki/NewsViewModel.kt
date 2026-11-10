package fi.uutishetki

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel(app: Application) : AndroidViewModel(app) {
    val opening = OpeningPolicy()
    val openingCompleted = MutableStateFlow(0L)
    fun foreground() {
        val generation = opening.started()
        viewModelScope.launch {
            repository.refresh(generation != null)
            if (generation != null) openingCompleted.value = generation
        }
    }
    val repository = (app as NewsApplication).repository
    private val preferences = app.getSharedPreferences("filters", 0)
    private val selected = MutableStateFlow(SelectionCodec.decode(preferences.getString("selection", null)))
    val selection = selected.asStateFlow()
    private fun select(value: Selection) {
        selected.value = value
        // Separate from refresh preferences; never cleared by a fetch or app update.
        preferences.edit().putString("selection", SelectionCodec.encode(value)).apply()
    }
    fun source(value: Source?) = select(selected.value.copy(source = value))
    fun toggle(topic: Topic) = select(selected.value.toggle(topic))
    fun allTopics() = select(selected.value.copy(topics = Topic.entries.toSet()))
    fun defaults() = select(Selection())
    fun refresh(force: Boolean = false) { viewModelScope.launch { repository.refresh(force) } }
}
