package org.watsi.uhp.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.uhp.activities.AuthenticationActivity
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.activities.ScanNewCardActivity
import org.watsi.uhp.activities.SearchByMemberCardActivity
import org.watsi.uhp.di.ActivityScope

@Module
abstract class ActivityModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ClinicActivityModule::class, FragmentModule::class])
    abstract fun bindMainActivity(): ClinicActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindAuthenticationActivity(): AuthenticationActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindScanNewCardActivity(): ScanNewCardActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindSearchByMemberCardActivity(): SearchByMemberCardActivity
}
