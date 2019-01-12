package org.watsi.uhp

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import org.watsi.uhp.di.DaggerAppComponent

class BaseApplication : DaggerApplication() {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }
}