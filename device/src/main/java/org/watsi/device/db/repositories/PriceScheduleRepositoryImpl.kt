package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.PriceScheduleApi
import org.watsi.device.db.daos.PriceScheduleDao
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.PriceScheduleRepository

class PriceScheduleRepositoryImpl(
        private val priceScheduleDao: PriceScheduleDao,
        private val api: CoverageApi,
        private val sessionManager: SessionManager
) : PriceScheduleRepository {

    override fun sync(delta: Delta): Completable {
        return sessionManager.currentToken()?.let { token ->
            priceScheduleDao.find(delta.modelId).flatMapCompletable { priceScheduleModel ->
                val priceSchedule = priceScheduleModel.toPriceSchedule()
                api.postPriceSchedule(token.getHeaderString(), token.user.providerId, PriceScheduleApi(priceSchedule))
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }
}
