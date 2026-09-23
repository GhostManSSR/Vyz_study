package BugBug.androidApp.ui.result

import BugBug.androidApp.ui.registration.RegistrationViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    vm: RegistrationViewModel
) {
    val player by vm.savedPlayer.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Данные игрока") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Назад") }
                }
            )
        }
    ) { padding ->
        val p = player
        if (p == null) {
            Column(
                Modifier.padding(padding).padding(24.dp).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Нет данных игрока", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack) { Text("Вернуться к форме") }
            }
            return@Scaffold
        }

        val fmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        Column(
            Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("ФИО: ${p.fullName}", style = MaterialTheme.typography.titleMedium)
            Text("Пол: ${p.gender.title}")
            Text("Курс: ${p.course}")
            Text("Сложность: ${p.difficulty} / 10")
            Text("Дата рождения: ${fmt.format(p.birthDate.time)}")

            Spacer(Modifier.height(16.dp))

            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(p.zodiac.iconRes),
                        contentDescription = p.zodiac.title,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Знак зодиака: ${p.zodiac.title}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Добро пожаловать, ${p.fullName.substringBefore(' ')}!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ты — ${p.zodiac.title}. Уровень: ${p.difficulty}/10. Готов к бою с жуками?",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("В главное меню")
            }
        }
    }
}