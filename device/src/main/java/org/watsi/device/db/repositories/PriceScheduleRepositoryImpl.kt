package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.PriceScheduleApi
import org.watsi.device.db.daos.PriceScheduleDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.PriceScheduleModel
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.PriceSchedule
import org.watsi.domain.repositories.PriceScheduleRepository
import java.util.UUID

class PriceScheduleRepositoryImpl(
    private val priceScheduleDao: PriceScheduleDao,
    private val api: CoverageApi,
    private val sessionManager: SessionManager,
    private val clock: Clock
) : PriceScheduleRepository {

    override fun find(id: UUID): Maybe<PriceSchedule> {
        return priceScheduleDao.find(id).map { it.toPriceSchedule() }.subscribeOn(Schedulers.io())
    }

    override fun create(priceSchedule: PriceSchedule, delta: Delta?): Completable {
        return Completable.fromAction {
            val priceScheduleModel = PriceScheduleModel.fromPriceSchedule(priceSchedule, clock)
            if (delta != null) {
                priceScheduleDao.insertWithDelta(
                    priceScheduleModel,
                    DeltaModel.fromDelta(delta, clock)
                )
            } else {
                priceScheduleDao.insert(priceScheduleModel)
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun sync(delta: Delta): Completable {
        return sessionManager.currentAuthenticationToken()?.let { token ->
            priceScheduleDao.find(delta.modelId).flatMapCompletable { priceScheduleModel ->
                val priceSchedule = priceScheduleModel.toPriceSchedule()
                api.postPriceSchedule(token.getHeaderString(), token.user.providerId, PriceScheduleApi(priceSchedule))
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }
}
