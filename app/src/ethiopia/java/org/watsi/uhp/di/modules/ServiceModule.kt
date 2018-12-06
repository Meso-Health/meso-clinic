package org.watsi.uhp.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.uhp.services.FetchMemberPhotosService
import org.watsi.uhp.services.FetchService
import org.watsi.uhp.services.SyncDataService

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun bindFetchService(): FetchService

    @ContributesAndroidInjector
    abstract fun bindSyncDataService(): SyncDataService

    @ContributesAndroidInjector
    abstract fun bindFetchMemberPhotoService(): FetchMemberPhotosService
}
