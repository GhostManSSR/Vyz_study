package BugBug.androidApp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import BugBug.androidApp.model.GameSettings
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    settings: GameSettings,
    onSettingsChange: (GameSettings) -> Unit
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
                        steps = 9
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
                        steps = 6
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
                        steps = 24
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
                        steps = 10
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