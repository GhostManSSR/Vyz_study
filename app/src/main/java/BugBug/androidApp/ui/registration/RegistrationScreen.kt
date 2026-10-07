package BugBug.androidApp.ui.registration

import BugBug.androidApp.R
import BugBug.androidApp.model.Author
import BugBug.androidApp.model.GameSettings
import BugBug.androidApp.model.Gender
import BugBug.androidApp.ui.authors.AuthorCard
import BugBug.androidApp.ui.settings.GameSettingsViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onSaved: () -> Unit,
    vm: RegistrationViewModel,
    settingsVm: GameSettingsViewModel
) {
    val state by vm.state.collectAsState()
    val settings by settingsVm.settings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val tabs = listOf("Регистрация", "Правила", "Авторы", "Настройки")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val selectedTab = pagerState.currentPage

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.onErrorShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Игрок") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                pageSpacing = 0.dp
            ) { pageIndex ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (pageIndex) {
                        0 -> RegistrationTabContent(
                            state = state,
                            vm = vm,
                            difficulty = settings.difficulty,
                            onSaved = onSaved
                        )
                        1 -> RulesTabContent()
                        2 -> AuthorsTabContent()
                        3 -> SettingsTabContent(
                            settings = settings,
                            onSettingsChange = { settingsVm.updateSettings(it) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegistrationTabContent(
    state: RegistrationUiState,
    vm: RegistrationViewModel,
    difficulty: Int,
    onSaved: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (state.isLoginMode) "Вход" else "Регистрация",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = { vm.toggleLoginMode() }) {
                    Text(
                        if (state.isLoginMode)
                            "Нет аккаунта? Регистрация"
                        else
                            "Есть аккаунт? Войти"
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.fullName,
                onValueChange = vm::onNameChange,
                label = { Text("ФИО") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrectEnabled = false
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = vm::onPasswordChange,
                label = { Text("Пароль") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(autoCorrectEnabled = false),
                isError = !state.isLoginMode && state.passwordError != null,
                supportingText = {
                    if (!state.isLoginMode && state.passwordError != null) {
                        Text(state.passwordError!!)
                    }
                }
            )
        }
    }

    if (!state.isLoginMode) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Пол",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = state.gender == Gender.MALE,
                        onClick = { vm.onGenderChange(Gender.MALE) }
                    )
                    Text("Мужской", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(
                        selected = state.gender == Gender.FEMALE,
                        onClick = { vm.onGenderChange(Gender.FEMALE) }
                    )
                    Text("Женский")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Курс",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = "${state.course} курс",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Курс") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        (1..4).forEach { c ->
                            DropdownMenuItem(
                                text = { Text("$c курс") },
                                onClick = {
                                    vm.onCourseChange(c)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Дата рождения",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))

                var showPicker by remember { mutableStateOf(false) }
                val datePickerState = rememberDatePickerState()

                OutlinedButton(
                    onClick = { showPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val c = state.birthDate
                    Text(
                        "${c.get(Calendar.DAY_OF_MONTH)}." +
                                "${c.get(Calendar.MONTH) + 1}." +
                                "${c.get(Calendar.YEAR)}"
                    )
                }

                if (showPicker) {
                    DatePickerDialog(
                        onDismissRequest = { showPicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val cal = Calendar.getInstance().apply {
                                        timeInMillis = millis
                                    }
                                    vm.onDateChange(cal)
                                }
                                showPicker = false
                            }) { Text("ОК") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPicker = false }) { Text("Отмена") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(state.zodiac.iconRes),
                        contentDescription = state.zodiac.title,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Знак зодиака: ${state.zodiac.title}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    Button(
        onClick = {
            if (state.isLoginMode) {
                vm.onLogin { onSaved() }
            } else {
                vm.onRegister(difficulty, onSaved)
            }
        },
        enabled = state.isFormValid,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (state.isLoginMode) "Войти" else "Зарегистрировать")
    }
}

@Composable
private fun RulesTabContent() {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Правила игры",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                """
                1. По экрану бегают жуки.
                2. Тапни по жуку — он уничтожен, +очки.
                3. Промахнулся — минус 5 очков.
                4. Игра длится заданное время.
                5. Цель — набрать максимум очков.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AuthorsTabContent() {
    val authors = listOf(
        Author("Лямин Т.", "ИП-316", "Fullstack", R.drawable.timofey),
        Author("Иванов А.", "ИП-316", "Fullstack", R.drawable.artem),
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Авторы проекта",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        authors.forEach { a ->
            AuthorCard(author = a)
        }
    }
}

@Composable
private fun SettingsTabContent(
    settings: GameSettings,
    onSettingsChange: (GameSettings) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Настройки игры",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Сложность: ${settings.difficulty} / 10",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = settings.difficulty.toFloat(),
                    onValueChange = {
                        onSettingsChange(settings.copy(difficulty = it.toInt()))
                    },
                    valueRange = 0f..10f,
                    steps = 9
                )
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Скорость игры: ${String.format("%.1f", settings.speed)}x",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = settings.speed,
                    onValueChange = { onSettingsChange(settings.copy(speed = it)) },
                    valueRange = 0.5f..2.0f,
                    steps = 6
                )
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Макс. насекомых: ${settings.maxCockroaches}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = settings.maxCockroaches.toFloat(),
                    onValueChange = {
                        onSettingsChange(settings.copy(maxCockroaches = it.toInt()))
                    },
                    valueRange = 5f..30f,
                    steps = 24
                )
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Бонусы каждые: ${settings.bonusIntervalSec} сек",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = settings.bonusIntervalSec.toFloat(),
                    onValueChange = {
                        onSettingsChange(settings.copy(bonusIntervalSec = it.toInt()))
                    },
                    valueRange = 10f..60f,
                    steps = 10
                )
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Длительность раунда: ${settings.roundDurationSec} сек",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = settings.roundDurationSec.toFloat(),
                    onValueChange = {
                        onSettingsChange(settings.copy(roundDurationSec = it.toInt()))
                    },
                    valueRange = 30f..180f,
                    steps = 15
                )
            }
        }
    }
}