package fi.uutishetki

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NewsViewModel(app: Application, private val saved: SavedStateHandle) : AndroidViewModel(app) {
    val repository = (app as NewsApplication).repository
    val source = saved.getStateFlow<String?>("source", null)
    val topic = saved.getStateFlow<String?>("topic", null)
    fun source(value: Source?) { saved["source"] = value?.name }
    fun topic(value: Topic?) { saved["topic"] = value?.name }
    fun clear() { source(null); topic(null) }
    fun refresh(force: Boolean = false) { viewModelScope.launch { repository.refresh(force) } }
}
