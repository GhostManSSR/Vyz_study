package BugBug.androidApp.ui.registration


import BugBug.androidApp.model.Gender
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onSaved: () -> Unit,
    vm: RegistrationViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.onErrorShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Регистрация игрока") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Секция: личные данные
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Личные данные",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.fullName,
                        onValueChange = vm::onNameChange,
                        label = { Text("ФИО") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    Text("Пол")
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

            // Секция: игровые настройки
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Игровые настройки",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)

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

                    Spacer(Modifier.height(12.dp))

                    Text("Сложность: ${state.difficulty} / 10")
                    Slider(
                        value = state.difficulty.toFloat(),
                        onValueChange = { vm.onDifficultyChange(it.toInt()) },
                        valueRange = 0f..10f,
                        steps = 9
                    )
                }
            }

            // Секция: дата и зодиак
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Дата рождения",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)

                    Spacer(Modifier.height(8.dp))

                    var showPicker by remember { mutableStateOf(false) }
                    val datePickerState = rememberDatePickerState()

                    OutlinedButton(
                        onClick = { showPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val c = state.birthDate
                        Text("${c.get(Calendar.DAY_OF_MONTH)}." +
                                "${c.get(Calendar.MONTH) + 1}." +
                                "${c.get(Calendar.YEAR)}")
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

            Button(
                onClick = { if (vm.onRegister()) onSaved() },
                enabled = state.isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Зарегистрировать")
            }
        }
    }
}