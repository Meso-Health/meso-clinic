package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.MemberRepository

class SyncMemberUseCase(
        private val memberRepository: MemberRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(): Completable {
        return Completable.fromAction {
            val unsyncedMemberDeltas = deltaRepository.unsynced(Delta.ModelName.MEMBER).blockingGet()

            unsyncedMemberDeltas.groupBy { it.modelId }.values.map { groupedDeltas ->
                memberRepository.sync(groupedDeltas).blockingGet()
                deltaRepository.markAsSynced(groupedDeltas).blockingGet()
            }
        }
    }
}
