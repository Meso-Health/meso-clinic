package org.watsi.uhp.di.modules

import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.PreferencesManagerImpl
import org.watsi.device.managers.SessionManager
import org.watsi.device.managers.SessionManagerImpl
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
}
