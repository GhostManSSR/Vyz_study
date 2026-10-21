package BugBug.androidApp.ui.game

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import BugBug.androidApp.R
import BugBug.androidApp.model.GameSettings
import BugBug.androidApp.model.InsectType
import kotlin.math.cos
import kotlin.math.sin

@Composable
private fun InsectSprite(
    type: InsectType,
    difficulty: Float,
    modifier: Modifier = Modifier
) {
    val shellColor = when (type) {
        InsectType.BEETLE -> Color(0xFFE53935)
        InsectType.FLY -> Color(0xFF78909C)
        InsectType.BUG -> Color(0xFFEF6C00)
        InsectType.WASP -> Color(0xFFFFCA28)
        InsectType.RHINO -> Color(0xFF5E35B1)
        InsectType.CATERPILLAR -> Color(0xFF43A047)
        InsectType.CENTIPEDE -> Color(0xFF66BB6A)
        InsectType.ARMORED_BEETLE -> Color(0xFF37474F)
        InsectType.SPIDER -> Color(0xFF512DA8)
        InsectType.GOLDEN -> Color(0xFFFFD54F)
    }
    val accentColor = when (type) {
        InsectType.BEETLE -> Color(0xFFFF8A80)
        InsectType.FLY -> Color(0xFFCFD8DC)
        InsectType.BUG -> Color(0xFFFFB74D)
        InsectType.WASP -> Color(0xFF212121)
        InsectType.RHINO -> Color(0xFFB39DDB)
        InsectType.CATERPILLAR -> Color(0xFF9CCC65)
        InsectType.CENTIPEDE -> Color(0xFF9CCC65)
        InsectType.ARMORED_BEETLE -> Color(0xFFB0BEC5)
        InsectType.SPIDER -> Color(0xFFFF5252)
        InsectType.GOLDEN -> Color(0xFFFFF176)
    }
    val darkColor = Color(0xFF263238)

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h / 2f
        val legWidth = (minOf(w, h) * 0.045f).coerceAtLeast(2.dp.toPx())

        fun leg(fromX: Float, fromY: Float, toX: Float, toY: Float) {
            drawLine(
                color = darkColor,
                start = Offset(fromX, fromY),
                end = Offset(toX, toY),
                strokeWidth = legWidth,
                cap = StrokeCap.Round
            )
        }

        when (type) {
            InsectType.CATERPILLAR -> {
                for (segment in 0..4) {
                    val x = w * (0.18f + segment * 0.16f)
                    drawCircle(
                        color = if (segment % 2 == 0) shellColor else accentColor,
                        radius = w * 0.14f,
                        center = Offset(x, h * 0.53f)
                    )
                    drawCircle(
                        color = Color(0xFF1B5E20),
                        radius = w * 0.024f,
                        center = Offset(x, h * 0.53f)
                    )
                    if (segment < 4) {
                        leg(x - w * 0.03f, h * 0.64f, x - w * 0.08f, h * 0.78f)
                        leg(x + w * 0.03f, h * 0.64f, x + w * 0.08f, h * 0.78f)
                    }
                }
                drawCircle(color = darkColor, radius = w * 0.018f, center = Offset(w * 0.12f, h * 0.48f))
                drawCircle(color = darkColor, radius = w * 0.018f, center = Offset(w * 0.17f, h * 0.48f))
                leg(w * 0.08f, h * 0.42f, w * 0.02f, h * 0.3f)
                leg(w * 0.16f, h * 0.42f, w * 0.11f, h * 0.29f)
            }

            InsectType.CENTIPEDE -> {
                val segments = 9
                for (segment in 0 until segments) {
                    val t = segment / (segments - 1f)
                    val x = w * (0.1f + 0.8f * t)
                    val radius = h * if (segment == segments - 1) 0.18f else 0.16f
                    drawCircle(
                        color = if (segment % 2 == 0) shellColor else accentColor,
                        radius = radius,
                        center = Offset(x, h * 0.5f)
                    )
                    if (segment < segments - 1) {
                        leg(x, h * 0.6f, x - w * 0.025f, h * 0.82f)
                        leg(x, h * 0.6f, x + w * 0.025f, h * 0.82f)
                        leg(x, h * 0.4f, x - w * 0.025f, h * 0.18f)
                        leg(x, h * 0.4f, x + w * 0.025f, h * 0.18f)
                    }
                }
                drawCircle(color = darkColor, radius = h * 0.026f, center = Offset(w * 0.88f, h * 0.45f))
                drawCircle(color = darkColor, radius = h * 0.026f, center = Offset(w * 0.92f, h * 0.45f))
                leg(w * 0.92f, h * 0.35f, w * 0.97f, h * 0.18f)
                leg(w * 0.88f, h * 0.35f, w * 0.83f, h * 0.18f)
            }

            else -> {
                if (type == InsectType.WASP || type == InsectType.FLY) {
                    drawOval(
                        color = Color.White.copy(alpha = 0.55f),
                        topLeft = Offset(w * 0.12f, h * 0.25f),
                        size = Size(w * 0.28f, h * 0.22f)
                    )
                    drawOval(
                        color = Color.White.copy(alpha = 0.55f),
                        topLeft = Offset(w * 0.6f, h * 0.25f),
                        size = Size(w * 0.28f, h * 0.22f)
                    )
                }

                if (type != InsectType.SPIDER) {
                    for (side in listOf(-1f, 1f)) {
                        for (legIndex in 0..2) {
                            val y = h * (0.43f + legIndex * 0.12f)
                            val startX = centerX + side * w * 0.18f
                            val endX = centerX + side * w * (0.36f + if (legIndex == 1) 0.05f else 0f)
                            leg(startX, y, endX, y + h * (if (legIndex == 1) 0.02f else 0.12f))
                        }
                    }
                }

                drawRoundRect(
                    color = shellColor,
                    topLeft = Offset(w * 0.25f, h * 0.32f),
                    size = Size(w * 0.5f, h * 0.52f),
                    cornerRadius = CornerRadius(w * 0.24f)
                )
                drawRoundRect(
                    color = darkColor,
                    topLeft = Offset(w * 0.25f, h * 0.31f),
                    size = Size(w * 0.5f, h * 0.08f),
                    cornerRadius = CornerRadius(w * 0.08f)
                )
                drawLine(
                    color = darkColor,
                    start = Offset(centerX, h * 0.38f),
                    end = Offset(centerX, h * 0.82f),
                    strokeWidth = legWidth,
                    cap = StrokeCap.Round
                )

                when (type) {
                    InsectType.BEETLE -> {
                        listOf(
                            Offset(w * 0.38f, h * 0.52f),
                            Offset(w * 0.62f, h * 0.52f),
                            Offset(w * 0.45f, h * 0.68f),
                            Offset(w * 0.58f, h * 0.68f)
                        ).forEach { spot ->
                            drawCircle(color = darkColor, radius = w * 0.04f, center = spot)
                        }
                    }

                    InsectType.WASP -> {
                        repeat(3) { stripe ->
                            drawRoundRect(
                                color = accentColor,
                                topLeft = Offset(w * 0.28f, h * (0.45f + stripe * 0.11f)),
                                size = Size(w * 0.44f, h * 0.05f),
                                cornerRadius = CornerRadius(w * 0.04f)
                            )
                        }
                    }

                    InsectType.FLY -> {
                        drawCircle(color = accentColor, radius = w * 0.08f, center = Offset(w * 0.39f, h * 0.44f))
                        drawCircle(color = accentColor, radius = w * 0.08f, center = Offset(w * 0.61f, h * 0.44f))
                    }

                    InsectType.RHINO -> {
                        val horn = Path().apply {
                            moveTo(centerX, h * 0.22f)
                            lineTo(centerX - w * 0.08f, h * 0.38f)
                            lineTo(centerX + w * 0.08f, h * 0.38f)
                            close()
                        }
                        drawPath(horn, color = accentColor)
                    }

                    InsectType.GOLDEN -> {
                        drawCircle(color = accentColor.copy(alpha = 0.8f), radius = w * 0.07f, center = Offset(w * 0.42f, h * 0.55f))
                        drawCircle(color = accentColor.copy(alpha = 0.65f), radius = w * 0.05f, center = Offset(w * 0.6f, h * 0.67f))
                    }

                    InsectType.BUG -> {
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(w * 0.31f, h * 0.56f),
                            size = Size(w * 0.38f, h * 0.1f),
                            cornerRadius = CornerRadius(w * 0.05f)
                        )
                    }

                    InsectType.ARMORED_BEETLE -> {
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(w * 0.29f, h * 0.4f),
                            size = Size(w * 0.42f, h * 0.12f),
                            cornerRadius = CornerRadius(w * 0.06f)
                        )
                        drawLine(
                            color = accentColor,
                            start = Offset(w * 0.32f, h * 0.66f),
                            end = Offset(w * 0.68f, h * 0.66f),
                            strokeWidth = legWidth * 1.5f,
                            cap = StrokeCap.Round
                        )
                    }

                    InsectType.SPIDER -> {
                        repeat(4) { mark ->
                            val y = h * (0.45f + mark * 0.09f)
                            for (side in listOf(-1f, 1f)) {
                                leg(
                                    centerX + side * w * 0.2f,
                                    y,
                                    centerX + side * w * 0.4f,
                                    y + if (mark % 2 == 0) h * 0.12f else -h * 0.08f
                                )
                            }
                        }
                        drawCircle(color = accentColor, radius = w * 0.035f, center = Offset(w * 0.43f, h * 0.27f))
                        drawCircle(color = accentColor, radius = w * 0.035f, center = Offset(w * 0.57f, h * 0.27f))
                    }

                    else -> Unit
                }

                drawCircle(color = darkColor, radius = w * 0.06f, center = Offset(centerX, h * 0.25f))
                drawCircle(color = Color.White, radius = w * 0.02f, center = Offset(centerX - w * 0.022f, h * 0.235f))
                drawCircle(color = Color.White, radius = w * 0.02f, center = Offset(centerX + w * 0.022f, h * 0.235f))
                leg(centerX - w * 0.03f, h * 0.18f, centerX - w * 0.12f, h * 0.08f)
                leg(centerX + w * 0.03f, h * 0.18f, centerX + w * 0.12f, h * 0.08f)
            }
        }
    }
}

private fun insectGooPalette(type: InsectType): Triple<Color, Color, Color> = when (type) {
    InsectType.CATERPILLAR, InsectType.CENTIPEDE -> Triple(
        Color(0xFF6FCF4A),
        Color(0xFF3F8F24),
        Color(0xFFA9F56A)
    )
    InsectType.WASP, InsectType.GOLDEN -> Triple(
        Color(0xFFE7D84A),
        Color(0xFFB79E24),
        Color(0xFFF7F19A)
    )
    else -> Triple(
        Color(0xFFA9D63D),
        Color(0xFF6B9D1F),
        Color(0xFFD5F36C)
    )
}

private fun gooSplatPath(center: Offset, radius: Float, rotationOffset: Float = 0f): Path {
    val multipliers = floatArrayOf(1f, 1.28f, 0.88f, 1.22f, 0.82f, 1.18f, 0.92f, 1.32f, 0.86f, 1.08f)
    val step = (2f * Math.PI.toFloat()) / multipliers.size
    return Path().apply {
        multipliers.forEachIndexed { index, factor ->
            val angle = rotationOffset + step * index
            val x = center.x + cos(angle) * radius * factor
            val y = center.y + sin(angle) * radius * factor
            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}

@Composable
private fun InsectSlapAnimation(
    effect: InsectHitEffect,
    density: androidx.compose.ui.unit.Density,
    modifier: Modifier = Modifier
) {
    val progress = remember(effect.id) { Animatable(0f) }
    LaunchedEffect(effect.id) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 650))
    }
    val animationProgress = progress.value
    val (fillColor, edgeColor, highlightColor) = insectGooPalette(effect.type)

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .size(96.dp)
                .offset(
                    x = with(density) { effect.position.x.toDp() } - 48.dp,
                    y = with(density) { effect.position.y.toDp() } - 48.dp
                )
                .graphicsLayer {
                    scaleX = 0.72f + animationProgress * 0.28f
                    scaleY = 0.72f + animationProgress * 0.28f
                    alpha = 1f - animationProgress * 0.55f
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.18f
            val shadowOffset = Offset(4.dp.toPx(), 6.dp.toPx())

            drawPath(
                path = gooSplatPath(center + shadowOffset, baseRadius * 1.22f, rotationOffset = 0.2f),
                color = edgeColor.copy(alpha = 0.28f)
            )
            drawPath(
                path = gooSplatPath(center, baseRadius * 1.18f, rotationOffset = 0.1f),
                color = edgeColor.copy(alpha = 0.9f)
            )
            drawPath(
                path = gooSplatPath(center, baseRadius, rotationOffset = 0.1f),
                color = fillColor
            )
            drawPath(
                path = gooSplatPath(
                    center = center - Offset(baseRadius * 0.18f, baseRadius * 0.2f),
                    radius = baseRadius * 0.48f,
                    rotationOffset = 0.35f
                ),
                color = highlightColor.copy(alpha = 0.65f)
            )

            val dropletAngles = listOf(0.15f, 0.95f, 1.85f, 2.55f, 3.55f, 4.15f, 5.1f)
            val dropletRadii = listOf(0.28f, 0.22f, 0.18f, 0.16f, 0.2f, 0.17f, 0.14f)
            dropletAngles.forEachIndexed { index, angle ->
                val distance = baseRadius * (1.7f + (index % 3) * 0.35f)
                val dropCenter = Offset(
                    x = center.x + cos(angle * Math.PI.toFloat()) * distance,
                    y = center.y + sin(angle * Math.PI.toFloat()) * distance
                )
                drawCircle(
                    color = edgeColor.copy(alpha = 0.88f),
                    radius = baseRadius * dropletRadii[index] * 1.2f,
                    center = dropCenter + Offset(1.5f, 2f)
                )
                drawCircle(
                    color = fillColor,
                    radius = baseRadius * dropletRadii[index],
                    center = dropCenter
                )
                drawCircle(
                    color = highlightColor.copy(alpha = 0.5f),
                    radius = baseRadius * dropletRadii[index] * 0.45f,
                    center = dropCenter - Offset(baseRadius * 0.06f, baseRadius * 0.06f)
                )
            }
        }
    }
}

@Composable
private fun BonusPickupAnimation(
    pickup: BonusPickup,
    density: androidx.compose.ui.unit.Density,
    modifier: Modifier = Modifier
) {
    val progress = remember(pickup.id) { Animatable(0f) }
    LaunchedEffect(pickup.id) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 700))
    }

    val iconRes = when (pickup.type) {
        BonusType.GRAVITY -> R.drawable.siclon
        BonusType.TIME -> R.drawable.clock
        BonusType.POINTS -> R.drawable.star
    }
    val label = when (pickup.type) {
        BonusType.GRAVITY -> "Гравитация!"
        BonusType.TIME -> "+10 сек"
        BonusType.POINTS -> "+${pickup.points}"
    }
    val animationProgress = progress.value

    Box(modifier = modifier) {
        Canvas(
            Modifier
                .size(96.dp)
                .offset(
                    x = with(density) { pickup.position.x.toDp() } - 48.dp,
                    y = with(density) { pickup.position.y.toDp() } - 48.dp
                )
                .graphicsLayer {
                    alpha = 1f - animationProgress
                    scaleX = 0.4f + animationProgress * 1.4f
                    scaleY = 0.4f + animationProgress * 1.4f
                }
        ) {
            val radius = size.minDimension * (0.18f + animationProgress * 0.3f)
            drawCircle(
                color = Color(0xFFFFEE58).copy(alpha = 1f - animationProgress),
                radius = radius * 1.4f,
                style = Stroke(width = 3.dp.toPx())
            )
            repeat(8) { index ->
                val angle = index * 45f * (Math.PI.toFloat() / 180f)
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 1f - animationProgress),
                    radius = 3.dp.toPx(),
                    center = Offset(
                        x = size.width / 2f + cos(angle) * radius,
                        y = size.height / 2f + sin(angle) * radius
                    )
                )
            }
        }
        Column(
            modifier = Modifier
                .offset(
                    x = with(density) { pickup.position.x.toDp() } - 36.dp,
                    y = with(density) { pickup.position.y.toDp() } - 38.dp
                )
                .graphicsLayer {
                    alpha = 1f - animationProgress
                    scaleX = 0.75f + animationProgress * 0.45f
                    scaleY = 0.75f + animationProgress * 0.45f
                    translationY = -animationProgress * 42.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

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
    val difficultyVisual by animateFloatAsState(
        targetValue = state.currentDifficulty / 10f,
        animationSpec = tween(durationMillis = 450),
        label = "difficulty-visual"
    )
    var fieldSize by remember { mutableStateOf(Size.Zero) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(state.lastAdaptiveMessage) {
        state.lastAdaptiveMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        // Строка 1: очки / жуки / время
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

                        // Строка 2: сложность + точность
                        Text(
                            "Сложность: ${state.currentDifficulty}/10  ·  Точность: ${
                                "%.0f".format(state.accuracy)
                            }%",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Строка 3: гравитация
                        if (state.gravityMode) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(R.drawable.siclon),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            // Фон
            Image(
                painter = painterResource(R.drawable.stol),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Canvas(Modifier.fillMaxSize()) {
                val intensity = difficultyVisual
                if (intensity > 0f) {
                    val difficultyColor = lerp(
                        Color(0xFF43A047),
                        Color(0xFFE53935),
                        intensity.coerceIn(0f, 1f)
                    )
                    val overlayAlpha = 0.08f + intensity * 0.18f
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                difficultyColor.copy(alpha = overlayAlpha * 0.7f),
                                Color.Transparent,
                                difficultyColor.copy(alpha = overlayAlpha)
                            ),
                            startY = 0f,
                            endY = size.height
                        )
                    )
                    drawRect(
                        color = Color(0xFFFF1A1A).copy(alpha = intensity * 0.9f),
                        topLeft = Offset(6f, 6f),
                        size = Size(size.width - 12f, size.height - 12f),
                        style = Stroke(width = 2f + intensity * 12f)
                    )
                }
            }

            // Старт игры
            LaunchedEffect(fieldSize, difficulty, settings) {
                if (fieldSize.width > 0f && !state.isRunning && !state.isGameOver) {
                    vm.startGame(
                        fieldSize = fieldSize,
                        difficulty = difficulty,
                        settings = settings
                    )
                }
            }

            // Перезапуск при повороте
            LaunchedEffect(isLandscape) {
                if (state.isRunning && fieldSize.width > 0f) {
                    val savedScore = state.score
                    val savedHits = state.hits
                    val savedMisses = state.misses
                    val savedTime = state.timeLeft
                    val savedGravity = state.gravityMode
                    val savedGravityTime = state.gravityTimeLeft
                    val savedWindowHits = state.adaptiveWindowHits
                    val savedWindowMisses = state.adaptiveWindowMisses
                    val savedAdapted = state.hasAdaptedThisRound

                    vm.stopGame()
                    vm.startGame(fieldSize, difficulty, settings)
                    vm.restoreGameState(
                        savedScore,
                        savedHits,
                        savedMisses,
                        savedTime,
                        savedWindowHits,
                        savedWindowMisses,
                        savedAdapted
                    )

                    if (savedGravity && savedGravityTime > 0) {
                        vm.enableGravityMode()
                        vm.setGravityTime(savedGravityTime)
                    }
                }
            }

            // Насекомые
            state.insects.forEach { insect ->
                val xDp = with(density) { insect.position.x.toDp() }
                val yDp = with(density) { insect.position.y.toDp() }
                val widthPx = if (insect.type == InsectType.CENTIPEDE) {
                    insect.size.width
                } else {
                    insect.size.width * (1f + difficultyVisual * 0.12f)
                }
                val heightPx = if (insect.type == InsectType.CENTIPEDE) {
                    insect.size.height * 0.72f
                } else {
                    insect.size.height * (1f + difficultyVisual * 0.12f)
                }
                val widthDp = with(density) { widthPx.toDp() }
                val heightDp = with(density) { heightPx.toDp() }

                Box(
                    modifier = Modifier
                        .size(width = widthDp, height = heightDp)
                        .offset(
                            x = xDp - (widthDp - with(density) { insect.size.width.toDp() }) / 2,
                            y = yDp - (heightDp - with(density) { insect.size.height.toDp() }) / 2
                        )
                        .graphicsLayer {
                            rotationZ = when (insect.type) {
                                InsectType.CATERPILLAR, InsectType.CENTIPEDE,
                                InsectType.ARMORED_BEETLE, InsectType.SPIDER ->
                                    insect.velocity.x * 0.08f
                                else -> insect.velocity.x * 0.35f
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (insect.type == InsectType.GOLDEN) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = Color(0xFFFFD700).copy(alpha = 0.24f),
                                    shape = CircleShape
                                )
                        )
                    }
                    InsectSprite(
                        type = insect.type,
                        difficulty = difficultyVisual,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            state.insectHitEffects.forEach { effect ->
                InsectSlapAnimation(
                    effect = effect,
                    density = density,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Бонусы
            val bonusSize = 40.dp
            val bonusHalf = bonusSize / 2

            state.bonuses.forEach { bonus ->
                val xDp = with(density) { bonus.position.x.toDp() }
                val yDp = with(density) { bonus.position.y.toDp() }

                val drawableRes = when (bonus.type) {
                    BonusType.GRAVITY -> R.drawable.siclon
                    BonusType.TIME    -> R.drawable.clock
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

            state.bonusPickup?.let { pickup ->
                BonusPickupAnimation(
                    pickup = pickup,
                    density = density,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Game Over
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