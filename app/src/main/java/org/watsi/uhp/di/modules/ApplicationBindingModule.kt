package org.watsi.uhp.di.modules

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import org.watsi.uhp.BaseApplication

@Module
abstract class ApplicationBindingModule {

    @Binds
    abstract fun application(baseApplication: BaseApplication): Application

    @Binds
    abstract fun context(baseApplication: BaseApplication): Context
}
