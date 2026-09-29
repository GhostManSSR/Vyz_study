package BugBug.androidApp

import android.app.Application
import BugBug.androidApp.data.local.GameDatabase
import BugBug.androidApp.data.repository.PlayerRepository

class BugGameApp : Application() {

    val database by lazy { GameDatabase.get(this) }
    val playerRepository by lazy { PlayerRepository(database.playerDao()) }
}