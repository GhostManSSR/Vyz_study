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
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    onBack: () -> Unit,
    vm: RegistrationViewModel = viewModel()
) {
    val player by vm.savedPlayer.collectAsState()
    val p = player ?: return

    val fmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

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

            Card {
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
        }
    }
}