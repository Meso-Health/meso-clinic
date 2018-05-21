package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.PhotoRepository

class SyncPhotoUseCase(
        private val photoRepository: PhotoRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(): Completable {
        return deltaRepository.unsyncedModelIds(Delta.ModelName.MEMBER, Delta.Action.ADD).flatMapCompletable {
            unsyncedMemberIds ->
            deltaRepository.unsynced(Delta.ModelName.PHOTO).flatMapCompletable { photoDeltas ->
                // filter out deltas that correspond to a Member that has not been synced yet
                val photoDeltasThatCanBeSynced = photoDeltas
                        .filter { !unsyncedMemberIds.contains(it.modelId) }
                        .groupBy { it.modelId }
                        .values
                Completable.concat(photoDeltasThatCanBeSynced.map { deltas ->
                    Completable.concat(listOf(
                            photoRepository.sync(deltas),
                            deltaRepository.markAsSynced(deltas)
                    ))
                })
            }
        }
    }
}
