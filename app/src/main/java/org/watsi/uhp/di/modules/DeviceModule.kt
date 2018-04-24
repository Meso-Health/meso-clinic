package org.watsi.uhp.di.modules

import android.accounts.AccountManager
import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock
import org.watsi.uhp.managers.PreferencesManager
import org.watsi.uhp.managers.SessionManager

@Module
class DeviceModule {

    @Provides
    fun provideClock(context: Context): Clock {
        AndroidThreeTen.init(context)
        return Clock.systemDefaultZone()
    }

    @Provides
    fun provideSessionManager(context: Context): SessionManager {
        return SessionManager(PreferencesManager(context), AccountManager.get(context))
    }
}
