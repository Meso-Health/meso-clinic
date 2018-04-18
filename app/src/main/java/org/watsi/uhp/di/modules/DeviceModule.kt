package org.watsi.uhp.di.modules

import dagger.Module
import dagger.Provides
import org.threeten.bp.Clock

@Module
class DeviceModule {

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
