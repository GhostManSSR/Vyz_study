package BugBug.androidApp.ui.game


import BugBug.androidApp.model.Insect
import BugBug.androidApp.domain.GameEngine
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GameUiState(
    val insects: List<Insect> = emptyList(),
    val score: Int = 0,
    val misses: Int = 0,
    val hits: Int = 0,
    val timeLeft: Int = 60,
    val isRunning: Boolean = false,
    val isGameOver: Boolean = false
)

class GameViewModel : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var gameLoop: Job? = null
    private var timerJob: Job? = null

    fun startGame(fieldSize: Size, difficulty: Int = 3) {
        if (fieldSize.width <= 0f) return

        val count = 6 + difficulty / 3 * 5

        _state.value = GameUiState(
            insects = GameEngine.spawnInsects(count, fieldSize, difficulty),
            timeLeft = 30,
            isRunning = true,
            isGameOver = false
        )

        gameLoop?.cancel()
        gameLoop = viewModelScope.launch {
            while (isActive && _state.value.isRunning) {
                delay(16)
                _state.update {
                    it.copy(insects = GameEngine.moveInsects(it.insects, fieldSize))
                }
            }
        }

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _state.value.timeLeft > 0 && _state.value.isRunning) {
                delay(1000)
                _state.update { it.copy(timeLeft = it.timeLeft - 1) }
            }
            if (_state.value.timeLeft <= 0) {
                _state.update { it.copy(isRunning = false, isGameOver = true) }
            }
        }
    }

    fun onTap(tap: Offset) {
        val s = _state.value
        if (!s.isRunning) return

        val hit = s.insects.firstOrNull { GameEngine.isHit(it, tap) }
        if (hit != null) {
            _state.update {
                it.copy(
                    insects = it.insects.filter { i -> i.id != hit.id },
                    score = it.score + hit.type.points,
                    hits = it.hits + 1
                )
            }
        } else {
            _state.update {
                it.copy(
                    score = (it.score - 5).coerceAtLeast(0),
                    misses = it.misses + 1
                )
            }
        }
    }

    fun stopGame() {
        gameLoop?.cancel()
        timerJob?.cancel()
        _state.update { it.copy(isRunning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopGame()
    }
}