package f.cking.software

import android.app.Application
import com.google.android.material.color.DynamicColors
import f.cking.software.data.DataModule
import f.cking.software.domain.interactor.InteractorsModule
import f.cking.software.domain.interactor.SaveFirstAppLaunchTimeInteractor
import f.cking.software.ui.UiModule
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import timber.log.Timber

class TheApp : Application() {

    override fun onCreate() {
        super.onCreate()
        applyDynamicColors()
        initDi()
        initTimber()
        saveFirstLaunchTime()
    }

    fun restartKoin() {
        stopKoin()
        initDi()
    }

    private fun applyDynamicColors() {
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    private fun saveFirstLaunchTime() {
        getKoin().get<SaveFirstAppLaunchTimeInteractor>().execute()
    }

    private fun initDi() {
        startKoin {
            androidContext(this@TheApp)
            modules(
                DataModule(SHARED_PREF_NAME, DATABASE_NAME).module,
                InteractorsModule.module,
                UiModule.module,
                module { single { this@TheApp } }
            )
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        const val SHARED_PREF_NAME = "app-prefs"
        const val DATABASE_NAME = "app-database"
    }
}