package BugBug.androidApp.di

import BugBug.androidApp.data.local.GameDatabase
import BugBug.androidApp.data.repository.PlayerRepository
import BugBug.androidApp.data.repository.SettingsRepository
import BugBug.androidApp.ui.game.GameViewModel
import BugBug.androidApp.ui.records.RecordsViewModel
import BugBug.androidApp.ui.registration.RegistrationViewModel
import BugBug.androidApp.ui.settings.GameSettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { GameDatabase.get(androidContext()) }
    single { get<GameDatabase>().playerDao() }
    single { PlayerRepository(get()) }
    single { SettingsRepository() }        // ← новое

    viewModel { RegistrationViewModel(get()) }
    viewModel { GameViewModel(get(), get(), androidApplication()) }  // ← 3 аргумента
    viewModel { RecordsViewModel(get()) }
    viewModel { GameSettingsViewModel(get()) }  // ← передаём SettingsRepository
}