package org.watsi.uhp.di.modules

import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import com.rollbar.android.Rollbar
import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.managers.Logger
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.PreferencesManagerImpl
import org.watsi.device.managers.SessionManager
import org.watsi.device.managers.SessionManagerImpl
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.managers.DebugLogger
import org.watsi.uhp.managers.RollbarLogger
import javax.inject.Singleton

@Module
class DeviceModule {

    @Provides
    fun provideClock(context: Context): Clock {
        AndroidThreeTen.init(context)
        return Clock.systemDefaultZone()
    }

    @Singleton
    @Provides
    fun providePreferencesManager(context: Context): PreferencesManager {
        return PreferencesManagerImpl(context)
    }

    @Singleton
    @Provides
    fun provideSessionManager(preferencesManager: PreferencesManager,
                              api: CoverageApi): SessionManager {
        return SessionManagerImpl(preferencesManager, api)
    }

    @Singleton
    @Provides
    fun provideLogger(context: Context): Logger {
        return if (BuildConfig.REPORT_TO_ROLLBAR) {
            val rollbarEnvironmentIdentifier = BuildConfig.FLAVOR + BuildConfig.BUILD_TYPE
            Rollbar.init(context, BuildConfig.ROLLBAR_API_KEY, rollbarEnvironmentIdentifier)
            RollbarLogger()
        } else {
            DebugLogger()
        }
    }
}
