package BugBug.androidApp.ui.game

import BugBug.androidApp.data.remote.GoldApiService
import BugBug.androidApp.data.remote.RetrofitClient
import BugBug.androidApp.data.repository.GoldRepository
import BugBug.androidApp.data.repository.PlayerRepository
import BugBug.androidApp.data.repository.SettingsRepository
import BugBug.androidApp.domain.AdaptiveDecision
import BugBug.androidApp.domain.AdaptiveRoundDecision
import BugBug.androidApp.domain.AdaptiveDifficultyEngine
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
    val bonusPickup: BonusPickup? = null,
    val insectHitEffects: List<InsectHitEffect> = emptyList(),
    val gravityMode: Boolean = false,
    val gravityTiltX: Float = 0f,
    val gravityTiltY: Float = 0f,
    val gravityTimeLeft: Int = 0,
    val goldRate: Double = 0.0,
    val currentDifficulty: Int = 5,
    val lastAdaptiveMessage: String? = null,
    val adaptiveModeEnabled: Boolean = false,
    val adaptiveWindowHits: Int = 0,
    val adaptiveWindowMisses: Int = 0,
    val hasAdaptedThisRound: Boolean = false
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

data class BonusPickup(
    val id: Int,
    val position: Offset,
    val type: BonusType,
    val points: Int
)

data class InsectHitEffect(
    val id: Long,
    val position: Offset,
    val type: InsectType
)

class GameViewModel(
    private val repository: PlayerRepository,
    private val settingsRepository: SettingsRepository,
    private val app: Application,
) : ViewModel() {



    private var goldenSpawnJob: Job? = null
    private val goldRepository = GoldRepository()
    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()
    private var gameFieldSize = Size.Zero
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
    private var hitEffectIdCounter = 0L

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
        gameFieldSize = fieldSize
        val startingDifficulty = difficulty.coerceIn(0, 10)

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
            bonusIntervalSec = settings.bonusIntervalSec,
            bonuses = emptyList(),
            gravityMode = false,
            currentDifficulty = startingDifficulty,
            lastAdaptiveMessage = null,
            adaptiveModeEnabled = settings.adaptiveModeEnabled
        )

        val initialInsects = spawnInsects(settings.maxCockroaches, fieldSize, startingDifficulty)
        _state.update { it.copy(insects = initialInsects) }

        gameLoop = viewModelScope.launch {
            while (isActive && _state.value.isRunning) {
                val speed = _state.value.gameSpeed
                delay((16 / speed).toLong())
                _state.update { currentState ->
                    val updated = if (currentState.gravityMode) {
                        GameEngine.moveInsectsWithGravity(
                            insects = currentState.insects,
                            fieldSize = fieldSize,
                            tiltX = currentState.gravityTiltX,
                            tiltY = currentState.gravityTiltY,
                            speedMultiplier = currentState.gameSpeed
                        )
                    } else {
                        GameEngine.moveInsects(
                            insects = currentState.insects,
                            fieldSize = fieldSize,
                            speedMultiplier = currentState.gameSpeed
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
                    val newInsects = spawnInsects(
                        toSpawn,
                        fieldSize,
                        currentState.currentDifficulty
                    )
                    _state.update { it.copy(insects = it.insects + newInsects) }
                }
            }
        }

        bonusJob = viewModelScope.launch {
            var elapsedSec = 0
            while (isActive && _state.value.isRunning) {
                delay(1000L)
                elapsedSec++
                val currentState = _state.value
                val now = System.currentTimeMillis()
                _state.update {
                    it.copy(bonuses = it.bonuses.filter { bonus ->
                        now - bonus.spawnTime < 10_000L
                    })
                }
                if (elapsedSec < currentState.bonusIntervalSec) continue
                elapsedSec = 0
                if (_state.value.isRunning) {
                    val bonus = spawnBonus(fieldSize)
                    _state.update { it.copy(bonuses = it.bonuses + bonus) }
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
                    val golden = GameEngine.spawnGoldenInsect(
                        fieldSize,
                        _state.value.currentDifficulty
                    )
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
            val firstSplitId = insectIdCounter + 1
            val replacementInsects = GameEngine.splitInsect(
                insect = hitInsect,
                fieldSize = gameFieldSize,
                firstId = firstSplitId
            )
            insectIdCounter += replacementInsects.size
            val gained = if (replacementInsects.isNotEmpty()) {
                0
            } else if (hitInsect.type == InsectType.GOLDEN) {
                (s.goldRate / 100.0).coerceAtLeast(10.0).toInt()
            } else {
                hitInsect.type.points
            }
            val hitEffect = InsectHitEffect(
                id = ++hitEffectIdCounter,
                position = tap,
                type = hitInsect.type
            )
            _state.update {
                it.copy(
                    insects = it.insects.filter { i -> i.id != hitInsect.id } + replacementInsects,
                    score = it.score + gained,
                    hits = it.hits + 1,
                    insectHitEffects = it.insectHitEffects + hitEffect
                )
            }
            viewModelScope.launch {
                delay(900L)
                _state.update { state ->
                    state.copy(insectHitEffects = state.insectHitEffects.filterNot { it.id == hitEffect.id })
                }
            }
            recordAdaptiveAttempt(hit = true)
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
        recordAdaptiveAttempt(hit = false)
    }

    private fun handleBonus(bonus: Bonus) {
        when (bonus.type) {
            BonusType.POINTS -> {
                _state.update {
                    it.copy(
                        bonuses = it.bonuses.filter { b -> b.id != bonus.id },
                        score = it.score + bonus.points,
                        bonusPickup = bonus.toPickup()
                    )
                }
            }
            BonusType.GRAVITY -> {
                _state.update {
                    it.copy(
                        bonuses = it.bonuses.filter { b -> b.id != bonus.id },
                        bonusPickup = bonus.toPickup()
                    )
                }
                enableGravityMode()
            }
            BonusType.TIME -> {
                _state.update {
                    it.copy(
                        bonuses = it.bonuses.filter { b -> b.id != bonus.id },
                        timeLeft = it.timeLeft + 10,
                        bonusPickup = bonus.toPickup()
                    )
                }
            }
        }
    }

    private fun Bonus.toPickup() = BonusPickup(
        id = id,
        position = position,
        type = type,
        points = points
    )

    private fun recordAdaptiveAttempt(hit: Boolean) {
        val current = _state.value
        if (!current.adaptiveModeEnabled) return

        val windowHits = current.adaptiveWindowHits + if (hit) 1 else 0
        val windowMisses = current.adaptiveWindowMisses + if (hit) 0 else 1
        if (windowHits + windowMisses < AdaptiveDifficultyEngine.ROUND_WINDOW_SIZE) {
            _state.value = current.copy(
                adaptiveWindowHits = windowHits,
                adaptiveWindowMisses = windowMisses
            )
            return
        }

        val currentSettings = settingsRepository.settings.value.copy(
            speed = current.gameSpeed,
            maxCockroaches = current.maxInsects,
            bonusIntervalSec = current.bonusIntervalSec,
            difficulty = current.currentDifficulty,
            adaptiveModeEnabled = true
        )
        when (
            val decision = AdaptiveDifficultyEngine.adjustDuringRound(
                current = currentSettings,
                hits = windowHits,
                misses = windowMisses
            )
        ) {
            is AdaptiveRoundDecision.Changed -> {
                settingsRepository.update(decision.newSettings)
                val message = buildRoundChangeMessage(
                    old = currentSettings,
                    new = decision.newSettings,
                    increased = decision.increased
                )
                _state.value = current.copy(
                    currentDifficulty = decision.newSettings.difficulty,
                    gameSpeed = decision.newSettings.speed,
                    maxInsects = decision.newSettings.maxCockroaches,
                    bonusIntervalSec = decision.newSettings.bonusIntervalSec,
                    insects = current.insects.take(decision.newSettings.maxCockroaches),
                    adaptiveWindowHits = 0,
                    adaptiveWindowMisses = 0,
                    hasAdaptedThisRound = true,
                    lastAdaptiveMessage = message
                )
            }
            is AdaptiveRoundDecision.NoChange -> {
                _state.value = current.copy(
                    adaptiveWindowHits = 0,
                    adaptiveWindowMisses = 0
                )
            }
        }
    }

    private fun buildRoundChangeMessage(
        old: GameSettings,
        new: GameSettings,
        increased: Boolean
    ): String = buildAdaptiveMessage(old, new, increased)

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
        timeLeft: Int,
        adaptiveWindowHits: Int = 0,
        adaptiveWindowMisses: Int = 0,
        hasAdaptedThisRound: Boolean = false
    ) {
        _state.update {
            it.copy(
                score = score,
                hits = hits,
                misses = misses,
                timeLeft = timeLeft,
                adaptiveWindowHits = adaptiveWindowHits,
                adaptiveWindowMisses = adaptiveWindowMisses,
                hasAdaptedThisRound = hasAdaptedThisRound
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
        val finalSettings = settings.copy(
            speed = s.gameSpeed,
            maxCockroaches = s.maxInsects,
            bonusIntervalSec = s.bonusIntervalSec,
            difficulty = s.currentDifficulty,
            adaptiveModeEnabled = s.adaptiveModeEnabled
        )
        viewModelScope.launch {
            repository.saveScore(
                playerId = playerId,
                score = s.score,
                hits = s.hits,
                misses = s.misses,
                difficulty = finalSettings.difficulty,
                roundDurationSec = finalSettings.roundDurationSec
            )

            if (finalSettings.adaptiveModeEnabled && !s.hasAdaptedThisRound) {
                val recent = repository.getRecentScores(playerId, limit = 5)
                val decision = AdaptiveDifficultyEngine.analyze(recent, finalSettings)

                when (decision) {
                    is AdaptiveDecision.Increase -> {
                        settingsRepository.update(decision.newSettings)
                        val msg = buildChangeMessage(
                            old = finalSettings,
                            new = decision.newSettings,
                            isIncrease = true
                        )
                        _state.update { it.copy(lastAdaptiveMessage = msg) }
                        android.util.Log.d("ADAPTIVE", msg)
                    }
                    is AdaptiveDecision.Decrease -> {
                        settingsRepository.update(decision.newSettings)
                        val msg = buildChangeMessage(
                            old = finalSettings,
                            new = decision.newSettings,
                            isIncrease = false
                        )
                        _state.update { it.copy(lastAdaptiveMessage = msg) }
                        android.util.Log.d("ADAPTIVE", msg)
                    }
                    is AdaptiveDecision.NoChange -> {
                        _state.update { it.copy(lastAdaptiveMessage = null) }
                        android.util.Log.d("ADAPTIVE", "➡ ${decision.reason}")
                    }
                }
            }
        }
    }

    private fun buildChangeMessage(
        old: GameSettings,
        new: GameSettings,
        isIncrease: Boolean
    ): String = buildAdaptiveMessage(old, new, isIncrease)

    private fun buildAdaptiveMessage(
        old: GameSettings,
        new: GameSettings,
        isIncrease: Boolean
    ): String {
        val arrow = if (isIncrease) "↑" else "↓"
        return "Сложность $arrow · скорость ${"%.1f".format(new.speed)}x · " +
                "жуки ${old.maxCockroaches}→${new.maxCockroaches}"
    }



    fun setGravityTime(seconds: Int) {
        _state.update { it.copy(gravityTimeLeft = seconds) }
    }
}