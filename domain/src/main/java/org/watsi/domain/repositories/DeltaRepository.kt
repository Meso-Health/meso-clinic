package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import org.watsi.domain.entities.Delta
import java.util.UUID

interface DeltaRepository {
    fun unsynced(modelName: Delta.ModelName): Single<List<Delta>>
    fun unsyncedModelIds(modelName: Delta.ModelName, action: Delta.Action): Single<List<UUID>>
    fun markAsSynced(deltas: List<Delta>): Completable
    fun syncStatus(): Flowable<SyncStatus>

    data class SyncStatus(
            val unsyncedNewMemberCount: Int? = null,
            val unsyncedEditedMemberCount: Int? = null,
            val unsyncedIdEventCount: Int? = null,
            val unsyncedEncounterCount: Int? = null,
            val unsyncedEncounterFormCount: Int? = null
    )
}
