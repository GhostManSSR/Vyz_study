package BugBug.androidApp.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import BugBug.androidApp.model.InsectType
import BugBug.androidApp.R
import androidx.compose.ui.layout.ContentScale
import BugBug.androidApp.model.GameSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onExit: () -> Unit,
    difficulty: Int = 3,
    settings: GameSettings = GameSettings(),
    vm: GameViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    var fieldSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Отслеживаем ориентацию экрана
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Очки: ${state.score} | Насекомых: ${state.insects.size}/${state.maxInsects} | ${state.timeLeft} сек",
                        fontSize = 14.sp,
                        maxLines = 1
                    )
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
                    // Пересоздаём игру только если размер значительно изменился
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
            // Фон
            Image(
                painter = painterResource(R.drawable.stol),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Запуск игры при известном размере поля
            LaunchedEffect(fieldSize, difficulty, settings) {
                if (fieldSize.width > 0f && !state.isRunning && !state.isGameOver) {
                    vm.startGame(
                        fieldSize = fieldSize,
                        difficulty = difficulty,
                        settings = settings
                    )
                }
            }

            // Перезапуск игры при изменении ориентации
            LaunchedEffect(isLandscape) {
                if (state.isRunning && fieldSize.width > 0f) {
                    // Сохраняем текущее состояние
                    val currentScore = state.score
                    val currentHits = state.hits
                    val currentMisses = state.misses
                    val currentTime = state.timeLeft

                    // Пересоздаём игру с новыми размерами
                    vm.stopGame()
                    vm.startGame(
                        fieldSize = fieldSize,
                        difficulty = difficulty,
                        settings = settings
                    )

                    // Восстанавливаем счёт и время
                    vm.restoreGameState(
                        score = currentScore,
                        hits = currentHits,
                        misses = currentMisses,
                        timeLeft = currentTime
                    )
                }
            }

            // Рендеринг насекомых
            state.insects.forEach { insect ->
                val drawableRes = when (insect.type) {
                    InsectType.BEETLE -> R.drawable.ic_insect
                    InsectType.FLY -> R.drawable.ic_insect3
                    InsectType.BUG -> R.drawable.ic_insect1
                }

                val xDp = with(density) { insect.position.x.toDp() }
                val yDp = with(density) { insect.position.y.toDp() }
                val sizeDp = with(density) { insect.size.width.toDp() }

                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(sizeDp)
                        .offset(x = xDp, y = yDp)
                )
            }

            // Рендеринг бонусов
            state.bonuses.forEach { bonus ->
                val xDp = with(density) { bonus.position.x.toDp() }
                val yDp = with(density) { bonus.position.y.toDp() }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = xDp, y = yDp)
                        .background(Color.Yellow, shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "+${bonus.points}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // Экран окончания игры
            if (state.isGameOver) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
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
                        Text("Макс. насекомых: ${state.maxInsects}")
                        Text("Длительность: ${settings.roundDurationSec} сек")
                        Text(
                            "Ориентация: ${if (isLandscape) "Альбомная" else "Портретная"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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