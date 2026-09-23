package BugBug.androidApp.ui.settings


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    difficulty: Int,
    soundEnabled: Boolean,
    onDifficultyChange: (Int) -> Unit,
    onSoundChange: (Boolean) -> Unit
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
            Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Сложность: $difficulty / 10",
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = difficulty.toFloat(),
                        onValueChange = { onDifficultyChange(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
            }

            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Звук", modifier = Modifier.weight(1f))
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = onSoundChange
                    )
                }
            }
        }
    }
}