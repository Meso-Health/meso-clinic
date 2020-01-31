package org.watsi.uhp.di.modules

import dagger.Module
import dagger.Provides
import org.watsi.device.managers.Logger
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.managers.NavigationManager

@Module
class ClinicActivityModule {

    @Provides
    fun provideNavigationManager(activity: ClinicActivity, logger: Logger): NavigationManager {
        return NavigationManager(activity.supportFragmentManager, R.id.fragment_container, logger)
    }
}
