package org.watsi.uhp.di.modules

import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import com.rollbar.android.Rollbar
import com.simprints.libsimprints.SimHelper
import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.managers.FingerprintManager
import org.watsi.device.managers.Logger
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.PreferencesManagerImpl
import org.watsi.device.managers.SessionManager
import org.watsi.device.managers.SessionManagerImpl
import org.watsi.device.managers.SimprintsManager
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.managers.AndroidKeyboardManager
import org.watsi.uhp.managers.DebugLogger
import org.watsi.uhp.managers.KeyboardManager
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
                              api: CoverageApi,
                              logger: Logger): SessionManager {
        return SessionManagerImpl(preferencesManager, api, logger)
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

    @Provides
    fun provideSimHelper(sessionManager: SessionManager): SimHelper {
        val userId = sessionManager.currentAuthenticationToken()!!.user.id
        return SimHelper(BuildConfig.SIMPRINTS_API_KEY, userId.toString())
    }

    @Provides
    fun provideFingerprintManager(simHelper: SimHelper, sessionManager: SessionManager): FingerprintManager {
        return SimprintsManager(simHelper, sessionManager)
    }

    @Provides
    fun provideKeyboardManager(context: Context): KeyboardManager {
        return AndroidKeyboardManager(context)
    }
}
