package BugBug.androidApp.ui.records

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import BugBug.androidApp.BugGameApp
import BugBug.androidApp.data.local.PlayerEntity
import BugBug.androidApp.data.local.ScoreRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class RecordsViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as BugGameApp).playerRepository

    val scores: StateFlow<List<ScoreRecord>> =
        repository.getTopScores()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val players: StateFlow<List<PlayerEntity>> =
        repository.getAllPlayers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}