package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.MemberRepository

class SyncMemberPhotoUseCase(
        private val memberRepository: MemberRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(): Completable {
        return Completable.fromAction {
            val unsyncedMemberPhotoDeltas = deltaRepository.unsynced(
                    Delta.ModelName.PHOTO).blockingGet()
            val unsyncedMemberIds = deltaRepository.unsyncedModelIds(
                    Delta.ModelName.MEMBER, Delta.Action.ADD).blockingGet()
            // the modelId in a photo delta corresponds to the member ID and not the photo ID
            // to make querying simpler
            val syncableMemberPhotoDeltas = unsyncedMemberPhotoDeltas
                    .filter { !unsyncedMemberIds.contains(it.modelId) }
                    .groupBy { it.modelId }
                    .values

            syncableMemberPhotoDeltas.map { groupedDeltas ->
                memberRepository.syncPhotos(groupedDeltas).blockingGet()
                deltaRepository.markAsSynced(groupedDeltas).blockingGet()
            }
        }
    }
}
