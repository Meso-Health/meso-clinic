package org.watsi.uhp

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import org.watsi.uhp.di.DaggerAppComponent
import org.watsi.uhp.managers.LocaleManager
import javax.inject.Inject

class BaseApplication : DaggerApplication() {
    @Inject lateinit var localeManager: LocaleManager

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }
}
