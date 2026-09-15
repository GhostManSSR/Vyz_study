package BugBug.androidApp.navigation

import BugBug.androidApp.ui.registration.RegistrationScreen
import BugBug.androidApp.ui.registration.RegistrationViewModel
import BugBug.androidApp.ui.result.ResultScreen
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val REGISTRATION = "registration"
    const val RESULT = "result"
}

@Composable
fun  AppNavHost(vm: RegistrationViewModel = viewModel()) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = Routes.REGISTRATION) {

        composable(Routes.REGISTRATION) {
            RegistrationScreen(
                onSaved = { nav.navigate(Routes.RESULT) },
                vm = vm
            )
        }

        composable(Routes.RESULT) {
            ResultScreen(
                onBack = { nav.popBackStack() },
                vm = vm
            )
        }
    }
}