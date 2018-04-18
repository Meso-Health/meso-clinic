package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.db.daos.DeltaDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository

class DeltaRepositoryImpl(
        private val deltaDao: DeltaDao,
        private val clock: Clock
) : DeltaRepository {

    override fun unsynced(modelName: Delta.ModelName): Single<List<Delta>> {
        return deltaDao.unsynced(modelName).map { deltaModels ->
            deltaModels.map { it.toDelta() }
        }.subscribeOn(Schedulers.io())
    }

    override fun markAsSynced(deltas: List<Delta>): Completable {
        return Completable.fromAction {
            deltaDao.update(deltas.map { DeltaModel.fromDelta(it.copy(synced = true), clock) })
        }.subscribeOn(Schedulers.io())
    }
}
