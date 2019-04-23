package org.watsi.uhp.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.uhp.services.DeleteSyncedPhotosService
import org.watsi.uhp.services.FetchPhotosService
import org.watsi.uhp.services.FetchDataService
import org.watsi.uhp.services.SyncDataService
import org.watsi.uhp.services.SyncPhotosService

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun bindFetchDataService(): FetchDataService

    @ContributesAndroidInjector
    abstract fun bindFetchMemberPhotoService(): FetchPhotosService

    @ContributesAndroidInjector
    abstract fun bindSyncDataService(): SyncDataService

    @ContributesAndroidInjector
    abstract fun bindSyncPhotosService(): SyncPhotosService

    @ContributesAndroidInjector
    abstract fun bindDeleteSyncedPhotoService(): DeleteSyncedPhotosService
}
