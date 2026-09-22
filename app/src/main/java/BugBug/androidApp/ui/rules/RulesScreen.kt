package BugBug.androidApp.ui.rules


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(onBack: () -> Unit) {
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Как играть", style = MaterialTheme.typography.headlineSmall)
            Text(
                """
                1. По экрану бегают жуки.
                2. Тапни по жуку — он уничтожен, начисляются очки.
                3. Промахнулся — минус 5 очков.
                4. Игра длится 60 секунд.
                5. Цель — набрать как можно больше очков.
                """.trimIndent()
            )

            Spacer(Modifier.height(16.dp))
            Text("Стоимость жуков", style = MaterialTheme.typography.headlineSmall)
            Text(
                """
                🪲 Жук — 10 очков
                🪰 Муха — 20 очков (быстрая)
                🐛 Клоп — 5 очков (медленный)
                """.trimIndent()
            )

            Spacer(Modifier.height(16.dp))
            Text("Уровни сложности", style = MaterialTheme.typography.headlineSmall)
            Text(
                """
                Лёгкий (1-3): 4-5 жуков, медленные
                Средний (4-6): 5-6 жуков, средняя скорость
                Сложный (7-10): 6-8 жуков, быстрые
                """.trimIndent()
            )
        }
    }
}