package BugBug.androidApp.ui.authors

import BugBug.androidApp.R
import BugBug.androidApp.model.Author
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorsScreen(onBack: () -> Unit) {
    val authors = listOf(
        Author("Лямин Т.", "ИП-316", "Fullstack", R.drawable.timofey),
        Author("Иванов А.", "ИП-316", "Fullstack", R.drawable.artem),
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
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(authors, key = { it.name }) { a ->
                AuthorCard(author = a)
            }
        }
    }
}

@Composable
fun AuthorCard(author: Author, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(20.dp)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Верхняя «шапка» с градиентом
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                colors.primaryContainer,
                                colors.secondaryContainer
                            )
                        )
                    )
            )

            // Контент карточки, приподнят над шапкой
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-36).dp)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Круглый аватар с рамкой
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(3.dp, colors.surface, CircleShape)
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(author.photoResId),
                            contentDescription = author.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    // Имя и роль — выравниваем по низу аватара
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = author.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 98.dp) // под аватаром
                ) {
                    InfoChip(
                        text = "Группа: ${author.group}",
                        container = colors.secondaryContainer,
                        content = colors.onSecondaryContainer
                    )
                    InfoChip(
                        text = author.role,
                        container = colors.tertiaryContainer,
                        content = colors.onTertiaryContainer
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun InfoChip(
    text: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = container
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = content,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}