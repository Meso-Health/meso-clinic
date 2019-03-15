package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.watsi.device.db.daos.ReferralDao
import org.watsi.domain.repositories.ReferralRepository

class ReferralRepositoryImpl(
    private val referralDao: ReferralDao,
    private val okHttpClient: OkHttpClient
) : ReferralRepository {
    override fun deleteAll(): Completable {
        return Completable.fromAction {
            okHttpClient.cache().evictAll()
            referralDao.deleteAll()
        }.subscribeOn(Schedulers.io())
    }
}