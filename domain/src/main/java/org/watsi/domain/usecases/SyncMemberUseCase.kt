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
        return deltaRepository.unsynced(Delta.ModelName.MEMBER).flatMapCompletable { memberDeltas ->
            Completable.concat(memberDeltas.groupBy { it.modelId }.values.map { groupedDeltas ->
                Completable.concat(listOf(
                        memberRepository.sync(groupedDeltas),
                        deltaRepository.markAsSynced(groupedDeltas)
                ))
            })
        }
    }
}
