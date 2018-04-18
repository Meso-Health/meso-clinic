package org.watsi.uhp.di

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import org.watsi.uhp.BaseApplication
import org.watsi.uhp.di.modules.ActivityModule
import org.watsi.uhp.di.modules.ApplicationBindingModule
import org.watsi.uhp.di.modules.DbModule
import org.watsi.uhp.di.modules.DeviceModule
import org.watsi.uhp.di.modules.ServiceModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ApplicationBindingModule::class,
    ActivityModule::class,
    ServiceModule::class,
    DeviceModule::class,
    DbModule::class
])
interface AppComponent : AndroidInjector<BaseApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<BaseApplication>()
}
