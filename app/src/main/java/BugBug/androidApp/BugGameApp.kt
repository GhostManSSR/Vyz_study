package BugBug.androidApp

import android.app.Application
import BugBug.androidApp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BugGameApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BugGameApp)
            modules(appModule)
        }
    }
}