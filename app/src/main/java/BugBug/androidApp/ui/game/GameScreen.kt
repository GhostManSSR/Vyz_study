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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import BugBug.androidApp.model.InsectType
import BugBug.androidApp.R
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onExit: () -> Unit,
    difficulty: Int = 3,
    vm: GameViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    var fieldSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Очки: ${state.score}  |  ${state.timeLeft} сек") },
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
                    fieldSize = Size(size.width.toFloat(), size.height.toFloat())
                }
                .pointerInput(fieldSize) {
                    detectTapGestures { offset -> vm.onTap(offset) }
                }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {}
            Image(
                painter = painterResource(R.drawable.stol),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            LaunchedEffect(fieldSize) {
                if (fieldSize.width > 0f && !state.isRunning && !state.isGameOver) {
                    vm.startGame(fieldSize, difficulty)
                }
            }

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
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (fieldSize.width > 0f) vm.startGame(fieldSize, difficulty)
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