package BugBug.androidApp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import BugBug.androidApp.model.GameSettings
import BugBug.androidApp.domain.AdaptiveDifficultyEngine
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    settings: GameSettings,
    onSettingsChange: (GameSettings) -> Unit,
    onAdaptiveModeChange: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Назад") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Адаптивная сложность",
                            style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (settings.adaptiveModeEnabled)
                                "Настройки меняются автоматически по результатам игр"
                            else
                                "Вы управляете настройками вручную",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.adaptiveModeEnabled,
                        onCheckedChange = { onAdaptiveModeChange(it) }
                    )
                }
            }
            if (settings.adaptiveModeEnabled) {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Как работает адаптация",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "Во время раунда система измеряет попадания, промахи и точность по " +
                                    "${AdaptiveDifficultyEngine.ROUND_WINDOW_SIZE} последним попыткам. " +
                                    "При точности не ниже ${AdaptiveDifficultyEngine.ROUND_HIGH_ACCURACY_THRESHOLD}% " +
                                    "и минимум ${AdaptiveDifficultyEngine.ROUND_MIN_HITS_FOR_INCREASE} попаданиях " +
                                    "сложность повышается; при точности не выше " +
                                    "${AdaptiveDifficultyEngine.ROUND_LOW_ACCURACY_THRESHOLD}% — понижается. " +
                                            "В замер входят попадания по насекомым и промахи, нажатия на бонусы " +
                                            "не считаются. В остальных случаях параметры не меняются.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "За одну оценку меняются уровень на 1, скорость на " +
                                    "${AdaptiveDifficultyEngine.SPEED_STEP}x, число насекомых на " +
                                    "${AdaptiveDifficultyEngine.COCKROACHES_STEP} и интервал бонусов на " +
                                    "${AdaptiveDifficultyEngine.BONUS_INTERVAL_STEP} с. " +
                                    "Границы: уровень ${AdaptiveDifficultyEngine.DIFFICULTY_MIN}–" +
                                    "${AdaptiveDifficultyEngine.DIFFICULTY_MAX}, скорость " +
                                    "${AdaptiveDifficultyEngine.SPEED_MIN}–${AdaptiveDifficultyEngine.SPEED_MAX}x, " +
                                    "насекомые ${AdaptiveDifficultyEngine.COCKROACHES_MIN}–" +
                                    "${AdaptiveDifficultyEngine.COCKROACHES_MAX}, бонусы " +
                                    "${AdaptiveDifficultyEngine.BONUS_INTERVAL_MIN}–" +
                                    "${AdaptiveDifficultyEngine.BONUS_INTERVAL_MAX} с.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Сложность: ${settings.difficulty} / 10",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Влияет на скорость и количество насекомых",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = settings.difficulty.toFloat(),
                        onValueChange = { newValue ->
                            onSettingsChange(settings.copy(difficulty = newValue.toInt()))
                        },
                        valueRange = 0f..10f,
                        steps = 9,
                        enabled = !settings.adaptiveModeEnabled
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0", style = MaterialTheme.typography.labelSmall)
                        Text("10", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Скорость игры: ${String.format("%.1f", settings.speed)}x",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Множитель скорости движения насекомых",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = settings.speed,
                        onValueChange = { newSpeed ->
                            onSettingsChange(settings.copy(speed = newSpeed))
                        },
                        valueRange = 0.5f..2.0f,
                        steps = 6,
                        enabled = !settings.adaptiveModeEnabled

                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.5x", style = MaterialTheme.typography.labelSmall)
                        Text("2.0x", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Тараканов на экране: ${settings.maxCockroaches}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Максимальное одновременное количество",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = settings.maxCockroaches.toFloat(),
                        onValueChange = { newValue ->
                            onSettingsChange(settings.copy(maxCockroaches = newValue.toInt()))
                        },
                        valueRange = 5f..30f,
                        steps = 24,
                        enabled = !settings.adaptiveModeEnabled
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("5", style = MaterialTheme.typography.labelSmall)
                        Text("30", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Бонусы каждые: ${settings.bonusIntervalSec} сек",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Как часто появляются бонусные предметы",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = settings.bonusIntervalSec.toFloat(),
                        onValueChange = { newValue ->
                            onSettingsChange(settings.copy(bonusIntervalSec = newValue.toInt()))
                        },
                        valueRange = 10f..60f,
                        steps = 10,
                        enabled = !settings.adaptiveModeEnabled
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10 сек", style = MaterialTheme.typography.labelSmall)
                        Text("60 сек", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Длительность раунда: ${settings.roundDurationSec} сек",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Время одной игровой сессии",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = settings.roundDurationSec.toFloat(),
                        onValueChange = { newValue ->
                            onSettingsChange(settings.copy(roundDurationSec = newValue.toInt()))
                        },
                        valueRange = 30f..180f,
                        steps = 15
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("30 сек", style = MaterialTheme.typography.labelSmall)
                        Text("180 сек", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    onSettingsChange(GameSettings())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сбросить настройки")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}