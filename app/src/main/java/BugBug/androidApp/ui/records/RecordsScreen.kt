package BugBug.androidApp.ui.records

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import BugBug.androidApp.data.local.PlayerEntity
import BugBug.androidApp.data.local.ScoreRecord
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    onBack: () -> Unit,
    scores: List<ScoreRecord>,
    players: List<PlayerEntity>,
    onSelectPlayer: (PlayerEntity) -> Unit,
    onNewPlayer: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Рекорды", "Игроки")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Рекорды") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Назад") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> RecordsList(scores)
                1 -> PlayersList(players, onSelectPlayer, onNewPlayer)
            }
        }
    }
}

@Composable
private fun RecordsList(scores: List<ScoreRecord>) {
    if (scores.isEmpty()) {
        EmptyState("Пока нет результатов")
        return
    }
    val fmt = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(scores) { index, r ->
            val position = index + 1
            val medalEmoji = when (position) {
                1 -> "  "
                2 -> "  "
                3 -> "  "
                else -> "  "
            }
            val positionColor = when (position) {
                1 -> Color(0xFFFFD700)
                2 -> Color(0xFFC0C0C0)
                3 -> Color(0xFFCD7F32)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Номер места + медаль
                    Text(
                        text = "$position",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = positionColor,
                        modifier = Modifier.width(36.dp)
                    )
                    Text(
                        text = medalEmoji,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Column(Modifier.weight(1f)) {
                        Text(r.fullName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Сложность: ${r.difficulty}/10  •  ${fmt.format(Date(r.playedAt))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        "${r.score}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayersList(
    players: List<PlayerEntity>,
    onSelect: (PlayerEntity) -> Unit,
    onNewPlayer: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = onNewPlayer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Новый игрок")
        }
        Spacer(Modifier.height(12.dp))

        if (players.isEmpty()) {
            EmptyState("Нет сохранённых игроков")
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(players) { p ->
                Card(
                    onClick = { onSelect(p) },
                    Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(p.fullName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${p.gender.title} • курс ${p.course} • сложность ${p.difficulty}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Знак: ${p.zodiacName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}