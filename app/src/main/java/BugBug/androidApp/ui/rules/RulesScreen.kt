package BugBug.androidApp.ui.rules

import BugBug.androidApp.data.repository.GoldRate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import BugBug.androidApp.model.InsectType
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    onBack: () -> Unit,
    vm: RulesViewModel = viewModel()
) {
    val goldRate by vm.goldRate.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Правила") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Назад") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RulesWelcomeCard()

            RulesSection(title = "Как играть") {
                NumberedRule(1, "По экрану бегают жуки.")
                NumberedRule(2, "Тапни по жуку — он уничтожен, начисляются очки.")
                NumberedRule(3, "Промахнулся — минус 5 очков.")
                NumberedRule(4, "Игра длится 60 секунд.")
                NumberedRule(5, "Цель — набрать как можно больше очков.")
                NumberedRule(6, "Иногда появляется золотой жук. Его цена = курс грамма золота ЦБ РФ на момент запуска раунда.")
            }

            RulesSection(title = "Стоимость жуков") {
                InsectRow(
                    insectType = InsectType.BEETLE,
                    name = "Божья коровка",
                    note = "обычный",
                    points = 5
                )
                InsectRow(
                    insectType = InsectType.FLY,
                    name = "Жук",
                    note = "быстрая",
                    points = 10
                )
                InsectRow(
                    insectType = InsectType.BUG,
                    name = "Клоп",
                    note = "медленный",
                    points = 20
                )
                InsectRow(
                    insectType = InsectType.WASP,
                    name = "Оса",
                    note = "обычный вид",
                    points = 15
                )
                InsectRow(
                    insectType = InsectType.RHINO,
                    name = "Жук-носорог",
                    note = "обычный вид",
                    points = 25
                )
                ScoringInfoCard()
                InsectRow(
                    insectType = InsectType.CATERPILLAR,
                    name = "Гусеница",
                    note = "потомство сороконожки",
                    points = 10
                )
                GoldBugRow(goldRate = goldRate)
            }

            RulesSection(title = "Разделяющиеся насекомые") {
                Text(
                    "Попади по большому насекомому один раз, чтобы разделить его. " +
                            "За него самого очки не начисляются.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SplitterRuleCard(
                    parent = "Сороконожка",
                    parentType = InsectType.CENTIPEDE,
                    level = "Сложность 7+",
                    children = listOf(
                        InsectType.CATERPILLAR to "+10",
                        InsectType.CATERPILLAR to "+10"
                    ),
                    accent = Color(0xFF43A047)
                )
                SplitterRuleCard(
                    parent = "Жук-щит",
                    parentType = InsectType.ARMORED_BEETLE,
                    level = "Сложность 7+",
                    children = listOf(InsectType.BUG to "+20", InsectType.BUG to "+20"),
                    accent = Color(0xFF546E7A)
                )
                SplitterRuleCard(
                    parent = "Паук",
                    parentType = InsectType.SPIDER,
                    level = "Сложность 9+",
                    children = listOf(InsectType.FLY to "+10", InsectType.FLY to "+10"),
                    accent = Color(0xFF7E57C2)
                )
            }

            RulesSection(title = "Уровни сложности") {
                DifficultyRow(
                    label = "Лёгкий",
                    range = "1–3",
                    detail = "4–5 жуков, медленные",
                    container = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer
                )
                DifficultyRow(
                    label = "Средний",
                    range = "4–6",
                    detail = "5–6 жуков, средняя скорость",
                    container = MaterialTheme.colorScheme.secondaryContainer,
                    content = MaterialTheme.colorScheme.onSecondaryContainer
                )
                DifficultyRow(
                    label = "Сложный",
                    range = "7–10",
                    detail = "Быстрые; жуки-щиты, с уровня 9 — пауки; рамка краснеет",
                    container = MaterialTheme.colorScheme.errorContainer,
                    content = MaterialTheme.colorScheme.onErrorContainer
                )
                DifficultyFeedbackCard()
            }
        }
    }
}

@Composable
private fun RulesWelcomeCard() {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(colors.primaryContainer, colors.secondaryContainer)
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Охота на жуков",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimaryContainer
            )
            Text(
                "Лови насекомых, собирай очки и следи за сложностью. " +
                        "Раздели большого жука, а затем поймай его потомство.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ScoringInfoCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "!",
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            "Гусеница приносит 10 очков. За сороконожку, жука-щит и паука очков нет — " +
                    "награда начисляется за их потомство.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
private fun SplitterRuleCard(
    parent: String,
    parentType: InsectType,
    level: String,
    children: List<Pair<InsectType, String>>,
    accent: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = accent.copy(alpha = 0.14f)
                ) {
                    Text(
                        level,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "за родителя: 0",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InsectNameBadge(
                    name = parent,
                    type = parentType,
                    accent = accent,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "→",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(
                    modifier = Modifier.weight(1.15f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    children.forEach { (type, points) ->
                        ChildInsectBadge(type = type, points = points, accent = accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun InsectNameBadge(
    name: String,
    type: InsectType,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RulesInsectIcon(type = type, modifier = Modifier.size(42.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
        Text(
            "1 попадание",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChildInsectBadge(type: InsectType, points: String, accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            type.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
        RulesInsectIcon(type = type, modifier = Modifier.size(30.dp))
        Text(
            points,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RulesInsectIcon(type: InsectType, modifier: Modifier = Modifier) {
    val shell = when (type) {
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
    val accent = when (type) {
        InsectType.BEETLE -> Color(0xFFFF8A80)
        InsectType.FLY -> Color(0xFFCFD8DC)
        InsectType.BUG -> Color(0xFFFFB74D)
        InsectType.WASP -> Color(0xFF212121)
        InsectType.RHINO -> Color(0xFFB39DDB)
        InsectType.CATERPILLAR, InsectType.CENTIPEDE -> Color(0xFF9CCC65)
        InsectType.ARMORED_BEETLE -> Color(0xFFB0BEC5)
        InsectType.SPIDER -> Color(0xFFFF5252)
        InsectType.GOLDEN -> Color(0xFFFFF176)
    }
    val dark = Color(0xFF263238)

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val legWidth = (size.minDimension * 0.055f).coerceAtLeast(1.5.dp.toPx())

        fun leg(from: Offset, to: Offset) {
            drawLine(dark, from, to, strokeWidth = legWidth, cap = StrokeCap.Round)
        }

        when (type) {
            InsectType.CATERPILLAR, InsectType.CENTIPEDE -> {
                val segments = if (type == InsectType.CATERPILLAR) 4 else 6
                val radius = if (type == InsectType.CATERPILLAR) h * 0.2f else h * 0.13f
                repeat(segments) { index ->
                    val x = w * (0.12f + 0.76f * index / (segments - 1f))
                    val y = h * 0.5f
                    if (type == InsectType.CENTIPEDE && index > 0) {
                        leg(Offset(x, y + radius * 0.5f), Offset(x - w * 0.04f, h * 0.88f))
                        leg(Offset(x, y - radius * 0.5f), Offset(x - w * 0.04f, h * 0.12f))
                    } else if (type == InsectType.CATERPILLAR && index > 0) {
                        leg(Offset(x, y + radius * 0.6f), Offset(x - w * 0.04f, h * 0.8f))
                    }
                    drawCircle(
                        color = if (index % 2 == 0) shell else accent,
                        radius = radius,
                        center = Offset(x, y)
                    )
                    if (type == InsectType.CATERPILLAR) {
                        drawCircle(dark, radius = radius * 0.12f, center = Offset(x, y))
                    }
                }
            }

            InsectType.SPIDER -> {
                repeat(4) { index ->
                    val y = h * (0.28f + index * 0.15f)
                    for (side in listOf(-1f, 1f)) {
                        leg(
                            Offset(center.x + side * w * 0.12f, y),
                            Offset(center.x + side * w * 0.46f, y + if (index % 2 == 0) h * 0.2f else -h * 0.16f)
                        )
                    }
                }
                drawCircle(shell, radius = w * 0.23f, center = Offset(center.x, h * 0.55f))
                drawCircle(shell, radius = w * 0.14f, center = Offset(center.x, h * 0.28f))
                drawCircle(accent, radius = w * 0.035f, center = Offset(center.x - w * 0.05f, h * 0.26f))
                drawCircle(accent, radius = w * 0.035f, center = Offset(center.x + w * 0.05f, h * 0.26f))
            }

            else -> {
                for (side in listOf(-1f, 1f)) {
                    repeat(3) { index ->
                        val y = h * (0.48f + index * 0.12f)
                        leg(
                            Offset(center.x + side * w * 0.15f, y),
                            Offset(center.x + side * w * 0.42f, y + h * 0.1f)
                        )
                    }
                }

                if (type == InsectType.WASP || type == InsectType.FLY) {
                    drawOval(
                        Color.White.copy(alpha = 0.75f),
                        topLeft = Offset(w * 0.08f, h * 0.19f),
                        size = Size(w * 0.34f, h * 0.2f)
                    )
                    drawOval(
                        Color.White.copy(alpha = 0.75f),
                        topLeft = Offset(w * 0.58f, h * 0.19f),
                        size = Size(w * 0.34f, h * 0.2f)
                    )
                }

                drawOval(
                    color = shell,
                    topLeft = Offset(w * 0.25f, h * 0.31f),
                    size = Size(w * 0.5f, h * 0.54f)
                )

                when (type) {
                    InsectType.BEETLE -> {
                        drawLine(dark, Offset(center.x, h * 0.34f), Offset(center.x, h * 0.82f), legWidth)
                        listOf(
                            Offset(w * 0.39f, h * 0.5f),
                            Offset(w * 0.61f, h * 0.5f),
                            Offset(w * 0.43f, h * 0.68f),
                            Offset(w * 0.57f, h * 0.68f)
                        ).forEach { drawCircle(dark, radius = w * 0.035f, center = it) }
                    }

                    InsectType.WASP -> repeat(3) { index ->
                        val y = h * (0.45f + index * 0.12f)
                        drawLine(accent, Offset(w * 0.31f, y), Offset(w * 0.69f, y), legWidth * 1.6f)
                    }

                    InsectType.FLY -> {
                        drawCircle(accent, radius = w * 0.075f, center = Offset(w * 0.4f, h * 0.44f))
                        drawCircle(accent, radius = w * 0.075f, center = Offset(w * 0.6f, h * 0.44f))
                    }

                    InsectType.RHINO -> {
                        leg(Offset(center.x, h * 0.33f), Offset(center.x, h * 0.08f))
                        leg(Offset(center.x, h * 0.1f), Offset(center.x - w * 0.1f, h * 0.3f))
                        leg(Offset(center.x, h * 0.1f), Offset(center.x + w * 0.1f, h * 0.3f))
                    }

                    InsectType.ARMORED_BEETLE -> {
                        drawRoundRect(
                            color = accent,
                            topLeft = Offset(w * 0.31f, h * 0.39f),
                            size = Size(w * 0.38f, h * 0.13f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.06f)
                        )
                        drawCircle(accent, radius = w * 0.035f, center = Offset(center.x, h * 0.66f))
                    }

                    InsectType.GOLDEN -> {
                        drawCircle(accent, radius = w * 0.07f, center = Offset(w * 0.42f, h * 0.53f))
                        drawCircle(accent, radius = w * 0.05f, center = Offset(w * 0.6f, h * 0.67f))
                    }

                    InsectType.BUG -> drawLine(
                        accent,
                        Offset(w * 0.33f, h * 0.61f),
                        Offset(w * 0.67f, h * 0.61f),
                        legWidth * 1.5f
                    )

                    else -> Unit
                }
                drawCircle(dark, radius = w * 0.045f, center = Offset(center.x, h * 0.26f))
            }
        }
    }
}

@Composable
private fun DifficultyFeedbackCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFFE53935)),
            contentAlignment = Alignment.Center
        ) {
            Text("!", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Text(
            "Чем выше сложность, тем ярче и толще становится красная рамка игрового поля.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

/* ---------- Секция ---------- */

@Composable
private fun RulesSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

/* ---------- Нумерованное правило ---------- */

@Composable
private fun NumberedRule(number: Int, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/* ---------- Строка обычного жука ---------- */

@Composable
private fun InsectRow(
    insectType: InsectType,
    name: String,
    note: String,
    points: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
        ) {
                RulesInsectIcon(type = insectType, modifier = Modifier.size(34.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "+$points",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/* ---------- Строка золотого жука (динамическая цена ЦБ) ---------- */

@Composable
private fun GoldBugRow(goldRate: GoldRate?) {
    val priceText = when {
        goldRate == null -> "загрузка…"
        goldRate.value <= 0.0 -> "нет данных"
        else -> String.format(
            Locale.getDefault(),
            "%,.0f очков",
            goldRate.value / 100
        ).replace('\u00A0', ' ')
    }

    val note = when {
        goldRate == null -> "цена ЦБ загружается…"
        goldRate.value <= 0.0 -> "проверьте подключение к интернету"
        else -> String.format(
            Locale.getDefault(),
            "бонус · %.2f ₽/г ЦБ РФ",
            goldRate.value / 100
        ).replace('\u00A0', ' ')
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFF8E1))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFECB3)),
            contentAlignment = Alignment.Center
        ) {
            RulesInsectIcon(type = InsectType.GOLDEN, modifier = Modifier.size(34.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = "Золотой жук",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF8D6E00)
            )
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFFFE082)
        ) {
            Text(
                text = priceText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/* ---------- Строка сложности ---------- */

@Composable
private fun DifficultyRow(
    label: String,
    range: String,
    detail: String,
    container: Color,
    content: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = content.copy(alpha = 0.15f)
        ) {
            Text(
                text = range,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = content,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = content
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = content.copy(alpha = 0.85f)
            )
        }
    }
}