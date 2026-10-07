package BugBug.androidApp.ui.game

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import BugBug.androidApp.R
import BugBug.androidApp.model.GameSettings
import BugBug.androidApp.model.InsectType
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onExit: () -> Unit,
    difficulty: Int = 3,
    currentPlayerId: Long = 0,
    settings: GameSettings = GameSettings(),
    vm: GameViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    var fieldSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        if (isLandscape) {
                            Text(
                                "Очки: ${state.score} | ${state.timeLeft} сек",
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        } else {
                            Text(
                                "Очки: ${state.score} | Жуков: ${state.insects.size}/${state.maxInsects} | ${state.timeLeft} сек",
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                        if (state.gravityMode) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(R.drawable.siclon),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)   // ← не 32
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "НАКЛОН: ${state.gravityTimeLeft} сек",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = {
                        vm.stopGame()
                        onExit()
                    }) { Text("← Выход") }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .onSizeChanged { size ->
                    val newSize = Size(size.width.toFloat(), size.height.toFloat())
                    if (fieldSize == Size.Zero ||
                        kotlin.math.abs(fieldSize.width - newSize.width) > 10f ||
                        kotlin.math.abs(fieldSize.height - newSize.height) > 10f) {
                        fieldSize = newSize
                    }
                }
                .pointerInput(fieldSize) {
                    detectTapGestures { offset -> vm.onTap(offset) }
                }
        ) {
            Image(
                painter = painterResource(R.drawable.stol),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            LaunchedEffect(fieldSize, difficulty, settings) {
                if (fieldSize.width > 0f && !state.isRunning && !state.isGameOver) {
                    vm.startGame(
                        fieldSize = fieldSize,
                        difficulty = difficulty,
                        settings = settings
                    )
                }
            }

            LaunchedEffect(isLandscape) {
                if (state.isRunning && fieldSize.width > 0f) {
                    val savedScore = state.score
                    val savedHits = state.hits
                    val savedMisses = state.misses
                    val savedTime = state.timeLeft
                    val savedGravity = state.gravityMode
                    val savedGravityTime = state.gravityTimeLeft

                    vm.stopGame()
                    vm.startGame(fieldSize, difficulty, settings)
                    vm.restoreGameState(savedScore, savedHits, savedMisses, savedTime)

                    if (savedGravity && savedGravityTime > 0) {
                        vm.enableGravityMode()
                        vm.setGravityTime(savedGravityTime)
                    }
                }
            }

            state.insects.forEach { insect ->
                val drawableRes = when (insect.type) {
                    InsectType.BEETLE -> R.drawable.ic_insect
                    InsectType.FLY -> R.drawable.ic_insect3
                    InsectType.BUG -> R.drawable.ic_insect1
                    InsectType.GOLDEN -> R.drawable.ic_insect4
                }

                val xDp = with(density) { insect.position.x.toDp() }
                val yDp = with(density) { insect.position.y.toDp() }
                val sizeDp = with(density) { insect.size.width.toDp() }

                Box(
                    modifier = Modifier
                        .size(sizeDp)
                        .offset(x = xDp, y = yDp),
                    contentAlignment = Alignment.Center
                ) {
                    if (insect.type == InsectType.GOLDEN) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = Color(0xFFFFD700).copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                    }
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            val bonusSize = 40.dp
            val bonusHalf = bonusSize / 2

            state.bonuses.forEach { bonus ->
                val xDp = with(density) { bonus.position.x.toDp() }
                val yDp = with(density) { bonus.position.y.toDp() }

                val drawableRes = when (bonus.type) {
                    BonusType.GRAVITY -> R.drawable.siclon
                    BonusType.POINTS  -> R.drawable.star
                }

                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(bonusSize)
                        .offset(
                            x = xDp - bonusHalf,
                            y = yDp - bonusHalf
                        )
                )
            }

            /*
            if (state.gravityMode) {
                Text(
                    "tilt X=${"%.1f".format(state.gravityTiltX)}, Y=${"%.1f".format(state.gravityTiltY)}",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    fontSize = 12.sp,
                    color = Color.Red
                )
            }
            */

            if (state.isGameOver) {
                LaunchedEffect(state.isGameOver) {
                    if (state.isGameOver && currentPlayerId > 0) {
                        vm.saveResult(currentPlayerId, settings)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    val padding = if (isLandscape) 16.dp else 24.dp

                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Игра окончена!",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Счёт: ${state.score}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Попаданий: ${state.hits}")
                        Text("Промахов: ${state.misses}")
                        Text("Точность: ${"%.1f".format(state.accuracy)}%")

                        if (!isLandscape) {
                            Text("Макс. насекомых: ${state.maxInsects}")
                            Text("Длительность: ${settings.roundDurationSec} сек")
                        }

                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (fieldSize.width > 0f) {
                                    vm.startGame(
                                        fieldSize = fieldSize,
                                        difficulty = difficulty,
                                        settings = settings
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Играть снова") }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                vm.stopGame()
                                onExit()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("В меню") }
                    }
                }
            }
        }
    }
}