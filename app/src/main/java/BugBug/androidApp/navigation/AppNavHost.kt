package BugBug.androidApp.navigation

import BugBug.androidApp.data.local.PlayerEntity
import BugBug.androidApp.model.ZodiacSign
import BugBug.androidApp.model.Player
import BugBug.androidApp.ui.authors.AuthorsScreen
import BugBug.androidApp.ui.game.GameScreen
import BugBug.androidApp.ui.menu.MenuScreen
import BugBug.androidApp.ui.records.RecordsScreen
import BugBug.androidApp.ui.records.RecordsViewModel
import BugBug.androidApp.ui.registration.RegistrationScreen
import BugBug.androidApp.ui.registration.RegistrationViewModel
import BugBug.androidApp.ui.result.ResultScreen
import BugBug.androidApp.ui.rules.RulesScreen
import BugBug.androidApp.ui.settings.GameSettingsViewModel
import BugBug.androidApp.ui.settings.SettingsScreen
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val REGISTRATION = "registration"
    const val RESULT       = "result"
    const val MENU         = "menu"
    const val GAME         = "game"
    const val RECORDS      = "records"
    const val AUTHORS      = "authors"
    const val RULES        = "rules"
    const val SETTINGS     = "settings"
}

@Composable
fun AppNavHost(
    regVm: RegistrationViewModel = viewModel(),
    settingsVm: GameSettingsViewModel = viewModel()
) {
    val nav = rememberNavController()
    val gameSettings by settingsVm.settings.collectAsState()
    val difficulty = gameSettings.difficulty
    val playerId by regVm.savedPlayerId.collectAsState()

    NavHost(nav, startDestination = Routes.REGISTRATION) {

        composable(Routes.REGISTRATION) {
            RegistrationScreen(
                onSaved = { nav.navigate(Routes.RESULT) },
                vm = regVm,
                settingsVm = settingsVm
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                onBack = { nav.popBackStack() },
                onContinue = {
                    nav.navigate(Routes.MENU) {
                        popUpTo(Routes.REGISTRATION) { inclusive = true }
                    }
                },
                vm = regVm
            )
        }

        composable(Routes.MENU) {
            val name = regVm.savedPlayer.collectAsState().value?.fullName
                ?.substringBefore(' ') ?: "игрок"
            MenuScreen(
                playerName = name,
                onPlay = { nav.navigate(Routes.GAME) },
                onRecords = { nav.navigate(Routes.RECORDS) },
                onChangePlayer = { nav.navigate(Routes.RECORDS) },
                onNewPlayer = {
                    nav.navigate(Routes.REGISTRATION) {
                        popUpTo(Routes.MENU) { inclusive = false }
                    }
                },
                onAuthors = { nav.navigate(Routes.AUTHORS) },
                onRules = { nav.navigate(Routes.RULES) },
                onSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.GAME) {
            GameScreen(
                onExit = { nav.popBackStack() },
                difficulty = difficulty,
                settings = gameSettings,
                currentPlayerId = playerId,
                vm = viewModel()
            )
        }

        composable(Routes.RECORDS) {
            val recordsVm: RecordsViewModel = viewModel()
            val scores by recordsVm.scores.collectAsState()
            val players by recordsVm.players.collectAsState()

            RecordsScreen(
                onBack = { nav.popBackStack() },
                scores = scores,
                players = players,
                onSelectPlayer = { p ->
                    regVm.loadExistingPlayer(p)
                    nav.navigate(Routes.MENU) {
                        popUpTo(Routes.REGISTRATION) { inclusive = true }
                    }
                },
                onNewPlayer = {
                    nav.navigate(Routes.REGISTRATION)
                }
            )
        }

        composable(Routes.AUTHORS) {
            AuthorsScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.RULES) {
            RulesScreen(onBack = { nav.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { nav.popBackStack() },
                settings = gameSettings,
                onSettingsChange = { settingsVm.updateSettings(it) }
            )
        }
    }
}