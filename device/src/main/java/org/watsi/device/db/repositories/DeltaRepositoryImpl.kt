package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.db.daos.DeltaDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import java.util.UUID

class DeltaRepositoryImpl(
    private val deltaDao: DeltaDao,
    private val clock: Clock
) : DeltaRepository {

    override fun insert(deltas: List<Delta>): Completable {
        val deltaModels = deltas.map { DeltaModel.fromDelta(it) }

        return Completable.fromAction {
            deltaDao.insert(deltaModels)
        }.subscribeOn(Schedulers.io())
    }

    override fun syncStatus(): Flowable<DeltaRepository.SyncStatus> {
        val syncFlowables = listOf(
            deltaDao.unsyncedCount(Delta.ModelName.MEMBER),
            deltaDao.unsyncedCount(Delta.ModelName.IDENTIFICATION_EVENT),
            deltaDao.unsyncedCount(Delta.ModelName.ENCOUNTER),
            deltaDao.unsyncedCount(Delta.ModelName.ENCOUNTER_FORM),
            deltaDao.unsyncedCount(Delta.ModelName.BILLABLE),
            deltaDao.unsyncedCount(Delta.ModelName.PRICE_SCHEDULE),
            deltaDao.unsyncedCount(Delta.ModelName.PHOTO)
        )

        return Flowable.combineLatest(syncFlowables) { results ->
            DeltaRepository.SyncStatus(
                unsyncedMembersCount = results[0] as Int,
                unsyncedIdEventsCount = results[1] as Int,
                unsyncedEncountersCount = results[2] as Int,
                unsyncedEncounterFormsCount = results[3] as Int,
                unsyncedBillablesCount = results[4] as Int,
                unsyncedPriceSchedulesCount = results[5] as Int,
                unsyncedPhotosCount = results[6] as Int
            )
        }
    }

    override fun unsynced(modelName: Delta.ModelName): Single<List<Delta>> {
        return deltaDao.unsynced(modelName).map { deltaModels ->
            deltaModels.map { it.toDelta() }
        }.subscribeOn(Schedulers.io())
    }

    override fun unsyncedModelIds(modelName: Delta.ModelName, action: Delta.Action): Single<List<UUID>> {
        return deltaDao.unsyncedModelIds(modelName, action).subscribeOn(Schedulers.io())
    }

    override fun markAsSynced(deltas: List<Delta>): Completable {
        return Completable.fromAction {
            deltaDao.update(deltas.map { DeltaModel.fromDelta(it.copy(synced = true), clock) })
        }.subscribeOn(Schedulers.io())
    }
}
