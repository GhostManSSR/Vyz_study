package BugBug.androidApp.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import BugBug.androidApp.R
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    playerName: String,
    onPlay: () -> Unit,
    onAuthors: () -> Unit,
    onRules: () -> Unit,
    onSettings: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Жуки") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_insect1),
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Привет, $playerName!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Нажми на жука — он уничтожен!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("▶  Играть", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onRules,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.document),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Правила", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onAuthors,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.authorization),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Авторы", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onSettings,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.maintenance),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Настройки", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}