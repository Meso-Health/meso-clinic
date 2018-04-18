package org.watsi.uhp.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.watsi.uhp.services.DeleteFetchedPhotoService
import org.watsi.uhp.services.DownloadMemberPhotosService
import org.watsi.uhp.services.FetchService
import org.watsi.uhp.services.SyncService

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun bindDeleteFetchedPhotoService(): DeleteFetchedPhotoService

    @ContributesAndroidInjector
    abstract fun bindDownloadMemberPhotoService(): DownloadMemberPhotosService

    @ContributesAndroidInjector
    abstract fun bindFetchService(): FetchService

    @ContributesAndroidInjector
    abstract fun bindSyncService(): SyncService
}
