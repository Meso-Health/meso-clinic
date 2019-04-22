package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.watsi.domain.entities.Delta
import java.util.UUID

interface DeltaRepository {
    fun insert(deltas: List<Delta>): Completable
    fun unsynced(modelName: Delta.ModelName): Single<List<Delta>>
    fun unsyncedModelIds(modelName: Delta.ModelName, action: Delta.Action): Single<List<UUID>>
    fun markAsSynced(deltas: List<Delta>): Completable
    fun syncStatus(): Flowable<SyncStatus>

    data class SyncStatus(
        val unsyncedMembersCount: Int,
        val unsyncedIdEventsCount: Int,
        val unsyncedEncountersCount: Int,
        val unsyncedEncounterFormsCount: Int,
        val unsyncedPriceSchedulesCount: Int,
        val unsyncedPhotosCount: Int
    )
}
