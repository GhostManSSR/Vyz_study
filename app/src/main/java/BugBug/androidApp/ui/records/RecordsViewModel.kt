package BugBug.androidApp.ui.records

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import BugBug.androidApp.BugGameApp
import BugBug.androidApp.data.local.PlayerEntity
import BugBug.androidApp.data.local.ScoreRecord
import BugBug.androidApp.data.repository.PlayerRepository
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RecordsViewModel(
    private val repository: PlayerRepository
) : ViewModel() {

    val scores: StateFlow<List<ScoreRecord>> =
        repository.getTopScores()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val players: StateFlow<List<PlayerEntity>> =
        repository.getAllPlayers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}