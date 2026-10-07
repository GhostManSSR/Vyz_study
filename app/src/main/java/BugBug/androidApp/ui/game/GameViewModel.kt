package BugBug.androidApp.ui.game

import BugBug.androidApp.data.remote.GoldApiService
import BugBug.androidApp.data.remote.RetrofitClient
import BugBug.androidApp.data.repository.GoldRepository
import BugBug.androidApp.data.repository.PlayerRepository
import android.app.Application
import BugBug.androidApp.domain.GameEngine
import BugBug.androidApp.model.GameSettings
import BugBug.androidApp.model.Insect
import BugBug.androidApp.model.InsectType
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
    val bonusIntervalSec: Int = 15,
    val bonuses: List<Bonus> = emptyList(),
    val gravityMode: Boolean = false,
    val gravityTiltX: Float = 0f,
    val gravityTiltY: Float = 0f,
    val gravityTimeLeft: Int = 0,
    val goldRate: Double = 0.0
) {
    val accuracy: Float
        get() = if (hits + misses == 0) 0f
        else hits.toFloat() / (hits + misses) * 100f
}

enum class BonusType { POINTS, GRAVITY, TIME }

data class Bonus(
    val id: Int,
    val position: Offset,
    val points: Int,
    val type: BonusType = BonusType.POINTS,
    val spawnTime: Long
)

class GameViewModel(
    private val repository: PlayerRepository,
    private val app: Application,
) : ViewModel() {



    private var goldenSpawnJob: Job? = null
    private val goldRepository = GoldRepository()
    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()
    private val bonusRadiusDp = 28f
    private val bonusRadiusPx: Float
        get() = bonusRadiusDp * app.resources.displayMetrics.density
    private var gameLoop: Job? = null
    private var timerJob: Job? = null
    private var spawnJob: Job? = null
    private var bonusJob: Job? = null
    private var gravityJob: Job? = null

    private var insectIdCounter: Long = 0
    private var bonusIdCounter = 0

    private var sensorController: SensorController? = null
    private var soundPlayer: SoundPlayer? = null
    private var gravityTimerJob: Job? = null

    init {
        viewModelScope.launch {
            val rate = goldRepository.getGoldRate()
            _state.update { it.copy(goldRate = rate.value) }
        }
    }
    fun startGame(
        fieldSize: Size,
        difficulty: Int = 3,
        settings: GameSettings
    ) {

        if (fieldSize.width <= 0f) return

        stopGame()

        if (soundPlayer == null) {
            soundPlayer =  SoundPlayer(app)
        }
        soundPlayer?.startBackgroundMusic()

        _state.value = GameUiState(
            insects = emptyList(),
            timeLeft = settings.roundDurationSec,
            isRunning = true,
            isGameOver = false,
            maxInsects = settings.maxCockroaches,
            gameSpeed = settings.speed,
            bonusIntervalSec = 15,
            bonuses = emptyList(),
            gravityMode = false
        )

        val initialInsects = spawnInsects(settings.maxCockroaches, fieldSize, difficulty)
        _state.update { it.copy(insects = initialInsects) }

        gameLoop = viewModelScope.launch {
            while (isActive && _state.value.isRunning) {
                delay((16 / settings.speed).toLong())
                _state.update { currentState ->
                    val updated = if (currentState.gravityMode) {
                        GameEngine.moveInsectsWithGravity(
                            insects = currentState.insects,
                            fieldSize = fieldSize,
                            tiltX = currentState.gravityTiltX,
                            tiltY = currentState.gravityTiltY,
                            speedMultiplier = settings.speed
                        )
                    } else {
                        GameEngine.moveInsects(
                            insects = currentState.insects,
                            fieldSize = fieldSize,
                            speedMultiplier = settings.speed
                        )
                    }
                    currentState.copy(insects = updated)
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

                    delay(10_000L)
                    _state.update {
                        it.copy(bonuses = it.bonuses.filter { b -> b.id != bonus.id })
                    }
                }
            }
        }


        goldenSpawnJob?.cancel()
        goldenSpawnJob = viewModelScope.launch {
            while (isActive && _state.value.isRunning) {
                delay(10_000L)
                if (_state.value.isRunning &&
                    _state.value.insects.size < _state.value.maxInsects &&
                    Math.random() < 0.6
                ) {
                    val golden = GameEngine.spawnGoldenInsect(fieldSize, difficulty)
                    _state.update { it.copy(insects = it.insects + golden) }
                }
            }
        }
    }

    private fun spawnInsects(
        count: Int,
        fieldSize: Size,
        difficulty: Int
    ): List<Insect> {
        return GameEngine.spawnInsects(
            count = count,
            fieldSize = fieldSize,
            difficulty = difficulty,
            speedMultiplier = _state.value.gameSpeed
        ).map { insect -> insect.copy(id = ++insectIdCounter) }
    }

    private fun spawnBonus(fieldSize: Size): Bonus {
        val x = fieldSize.width * (0.1f + Math.random().toFloat() * 0.8f)
        val y = fieldSize.height * (0.1f + Math.random().toFloat() * 0.8f)

        val type = when {
            Math.random() < 0.33 -> BonusType.GRAVITY
            Math.random() < 0.5  -> BonusType.TIME
            else                 -> BonusType.POINTS
        }

        return Bonus(
            id = ++bonusIdCounter,
            position = Offset(x, y),
            points = (10..50).random(),
            type = type,
            spawnTime = System.currentTimeMillis()
        )
    }

    fun onTap(tap: Offset) {
        val s = _state.value
        if (!s.isRunning) return

        val hitInsect = s.insects.firstOrNull { GameEngine.isHit(it, tap) }
        if (hitInsect != null) {
            val gained = if (hitInsect.type == InsectType.GOLDEN) {
                (s.goldRate / 100.0).coerceAtLeast(10.0).toInt()
            } else {
                hitInsect.type.points
            }
            android.util.Log.d("TAP_DEBUG", "gained=$gained")
            _state.update {
                it.copy(
                    insects = it.insects.filter { i -> i.id != hitInsect.id },
                    score = it.score + gained,
                    hits = it.hits + 1
                )
            }
            return
        }

        val hitBonus = s.bonuses.firstOrNull { b ->
            val dx = b.position.x - tap.x
            val dy = b.position.y - tap.y
            (dx * dx + dy * dy) < 10_000f
        }

        if (hitBonus != null) {
            handleBonus(hitBonus)
            return
        }

        _state.update {
            it.copy(
                score = (it.score - 5).coerceAtLeast(0),
                misses = it.misses + 1
            )
        }
    }

    private fun handleBonus(bonus: Bonus) {
        when (bonus.type) {
            BonusType.POINTS -> {
                _state.update {
                    it.copy(
                        bonuses = it.bonuses.filter { b -> b.id != bonus.id },
                        score = it.score + bonus.points,
                        hits = it.hits + 1
                    )
                }
            }
            BonusType.GRAVITY -> {
                _state.update {
                    it.copy(bonuses = it.bonuses.filter { b -> b.id != bonus.id })
                }
                enableGravityMode()
            }
            BonusType.TIME -> {
                _state.update {
                    it.copy(
                        bonuses = it.bonuses.filter { b -> b.id != bonus.id },
                        timeLeft = it.timeLeft + 10,
                        hits = it.hits + 1
                    )
                }
            }
        }
    }

    fun enableGravityMode() {
        val ctx = app

        if (sensorController == null) {
            sensorController = SensorController(ctx).also { it.start() }
        }
        if (soundPlayer == null) {
            soundPlayer = SoundPlayer(ctx)
        }
        soundPlayer?.playScream()

        _state.update {
            it.copy(
                gravityMode = true,
                gravityTimeLeft = 10
            )
        }

        gravityJob?.cancel()
        gravityJob = viewModelScope.launch {
            while (isActive && _state.value.gravityMode && _state.value.isRunning) {
                val s = sensorController ?: break
                _state.update {
                    it.copy(gravityTiltX = s.tiltX, gravityTiltY = s.tiltY)
                }
                delay(50)
            }
        }

        gravityTimerJob?.cancel()
        gravityTimerJob = viewModelScope.launch {
            while (isActive && _state.value.gravityTimeLeft > 0) {
                delay(1000)
                _state.update { it.copy(gravityTimeLeft = it.gravityTimeLeft - 1) }
            }
            if (_state.value.gravityTimeLeft <= 0) {
                disableGravityMode()
            }
        }
    }

    fun disableGravityMode() {
        gravityJob?.cancel()
        gravityTimerJob?.cancel()
        sensorController?.stop()
        sensorController = null
        soundPlayer?.release()
        soundPlayer = null
        _state.update {
            it.copy(
                gravityMode = false,
                gravityTimeLeft = 0
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
        goldenSpawnJob?.cancel()
        bonusJob?.cancel()
        disableGravityMode()

        soundPlayer?.stopBackgroundMusic()
        soundPlayer?.release()
        soundPlayer = null

        _state.update { it.copy(isRunning = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopGame()
    }

    fun saveResult(playerId: Long, settings: GameSettings) {
        val s = _state.value
        viewModelScope.launch {
            repository.saveScore(
                playerId = playerId,
                score = s.score,
                hits = s.hits,
                misses = s.misses,
                difficulty = settings.difficulty,
                roundDurationSec = settings.roundDurationSec
            )
        }
    }

    fun setGravityTime(seconds: Int) {
        _state.update { it.copy(gravityTimeLeft = seconds) }
    }
}