package BugBug.androidApp.ui.game

import BugBug.androidApp.model.Insect
import BugBug.androidApp.domain.GameEngine
import BugBug.androidApp.model.GameSettings
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
    val isGameOver: Boolean = false,
    val maxInsects: Int = 10,
    val gameSpeed: Float = 1.0f,
    val bonusIntervalSec: Int = 30,
    val bonuses: List<Bonus> = emptyList()
)

data class Bonus(
    val id: Int,
    val position: Offset,
    val points: Int,
    val spawnTime: Long
)

class GameViewModel : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private var gameLoop: Job? = null
    private var timerJob: Job? = null
    private var spawnJob: Job? = null
    private var bonusJob: Job? = null

    private var insectIdCounter: Long = 0
    private var bonusIdCounter = 0

    fun startGame(
        fieldSize: Size,
        difficulty: Int = 3,
        settings: GameSettings
    ) {
        if (fieldSize.width <= 0f) return

        stopGame()

        _state.value = GameUiState(
            insects = emptyList(),
            timeLeft = settings.roundDurationSec,
            isRunning = true,
            isGameOver = false,
            maxInsects = settings.maxCockroaches,
            gameSpeed = settings.speed,
            bonusIntervalSec = settings.bonusIntervalSec,
            bonuses = emptyList()
        )

        val initialInsects = spawnInsects(settings.maxCockroaches, fieldSize, difficulty)
        _state.update { it.copy(insects = initialInsects) }

        gameLoop = viewModelScope.launch {
            while (isActive && _state.value.isRunning) {
                delay((16 / settings.speed).toLong())
                _state.update { currentState ->
                    currentState.copy(
                        insects = GameEngine.moveInsects(currentState.insects, fieldSize, settings.speed)
                    )
                }
            }
        }

        timerJob = viewModelScope.launch {
            while (isActive && _state.value.timeLeft > 0 && _state.value.isRunning) {
                delay(1000)
                _state.update { it.copy(timeLeft = it.timeLeft - 1) }
            }
            if (_state.value.timeLeft <= 0 && _state.value.isRunning) {
                _state.update { it.copy(isRunning = false, isGameOver = true) }
            }
        }

        spawnJob = viewModelScope.launch {
            while (isActive && _state.value.isRunning) {
                delay(500)
                val currentState = _state.value
                val currentCount = currentState.insects.size

                if (currentCount < currentState.maxInsects) {
                    val toSpawn = (currentState.maxInsects - currentCount).coerceAtMost(3)
                    val newInsects = spawnInsects(toSpawn, fieldSize, difficulty)
                    _state.update { it.copy(insects = it.insects + newInsects) }
                }
            }
        }

        bonusJob = viewModelScope.launch {
            while (isActive && _state.value.isRunning) {
                delay(settings.bonusIntervalSec * 1000L)
                if (_state.value.isRunning) {
                    val bonus = spawnBonus(fieldSize)
                    _state.update { it.copy(bonuses = it.bonuses + bonus) }

                    kotlinx.coroutines.delay(10000)
                    _state.update {
                        it.copy(bonuses = it.bonuses.filter { b -> b.id != bonus.id })
                    }
                }
            }
        }
    }

    private fun spawnInsects(count: Int, fieldSize: Size, difficulty: Int): List<Insect> {
        return GameEngine.spawnInsects(
            count = count,
            fieldSize = fieldSize,
            difficulty = difficulty,
            speedMultiplier = _state.value.gameSpeed
        ).map { insect ->
            insect.copy(id = ++insectIdCounter)
        }
    }

    private fun spawnBonus(fieldSize: Size): Bonus {
        val x = (fieldSize.width * (0.1f + Math.random().toFloat() * 0.8f))
        val y = (fieldSize.height * (0.1f + Math.random().toFloat() * 0.8f))

        return Bonus(
            id = ++bonusIdCounter,
            position = Offset(x, y),
            points = (10..50).random(),
            spawnTime = System.currentTimeMillis()
        )
    }

    fun onTap(tap: Offset) {
        val s = _state.value
        if (!s.isRunning) return

        val hitInsect = s.insects.firstOrNull { GameEngine.isHit(it, tap) }
        if (hitInsect != null) {
            _state.update {
                it.copy(
                    insects = it.insects.filter { i -> i.id != hitInsect.id },
                    score = it.score + hitInsect.type.points,
                    hits = it.hits + 1
                )
            }
            return
        }

        val hitBonus = s.bonuses.firstOrNull {
            val dx = it.position.x - tap.x
            val dy = it.position.y - tap.y
            (dx * dx + dy * dy) < 2500
        }
        if (hitBonus != null) {
            _state.update {
                it.copy(
                    bonuses = it.bonuses.filter { b -> b.id != hitBonus.id },
                    score = it.score + hitBonus.points,
                    hits = it.hits + 1
                )
            }
            return
        }

        _state.update {
            it.copy(
                score = (it.score - 5).coerceAtLeast(0),
                misses = it.misses + 1
            )
        }
    }

    fun restoreGameState(
        score: Int,
        hits: Int,
        misses: Int,
        timeLeft: Int
    ) {
        _state.update {
            it.copy(
                score = score,
                hits = hits,
                misses = misses,
                timeLeft = timeLeft
            )
        }
    }

    fun stopGame() {
        gameLoop?.cancel()
        timerJob?.cancel()
        spawnJob?.cancel()
        bonusJob?.cancel()
        _state.update { it.copy(isRunning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopGame()
    }
}