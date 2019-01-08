package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.MemberRepository

class SyncMemberPhotoUseCase(
        private val memberRepository: MemberRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedMemberPhotoDeltas = deltaRepository.unsynced(
                    Delta.ModelName.PHOTO).blockingGet()
            // the modelId in a photo delta corresponds to the member ID and not the photo ID
            // to make querying simpler
            val syncableMemberPhotoDeltas = unsyncedMemberPhotoDeltas
                    .groupBy { it.modelId }
                    .values

            syncableMemberPhotoDeltas.map { groupedDeltas ->
                Completable.concatArray(
                    memberRepository.syncPhotos(groupedDeltas),
                    deltaRepository.markAsSynced(groupedDeltas)
                ).onErrorComplete {
                    onError(it)
                }.blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}
