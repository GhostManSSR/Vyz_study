package BugBug.androidApp.ui.authors


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Author(val name: String, val group: String, val role: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorsScreen(onBack: () -> Unit) {
    val authors = listOf(
        Author("Лямин Т.", "ИП-316", "Fullstack"),
        Author("Иванов А.", "ИП-316", "Fullstack"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Авторы") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Назад") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(authors) { a ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(a.name, style = MaterialTheme.typography.titleMedium)
                        Text("Группа: ${a.group}")
                        Text("Роль: ${a.role}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}