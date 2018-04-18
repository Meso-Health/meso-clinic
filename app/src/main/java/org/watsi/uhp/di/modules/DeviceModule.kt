package org.watsi.uhp.di.modules

import android.content.Context
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock

@Module
class DeviceModule {

    @Provides
    fun provideClock(context: Context): Clock {
        AndroidThreeTen.init(context)
        return Clock.systemDefaultZone()
    }
}
