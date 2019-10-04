package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.watsi.device.db.AppDatabase
import org.watsi.domain.repositories.MainRepository

class MainRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val okHttpClient: OkHttpClient
): MainRepository {
    override fun deleteAllUserData(): Completable {
        return Completable.fromAction {
            appDatabase.clearAllTables()
            okHttpClient.cache()?.evictAll()
        }.subscribeOn(Schedulers.io())
    }
}
