package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.domain.entities.Delta

interface DeltaRepository {
    fun unsynced(modelName: Delta.ModelName): Single<List<Delta>>
    fun markAsSynced(deltas: List<Delta>): Completable
}
