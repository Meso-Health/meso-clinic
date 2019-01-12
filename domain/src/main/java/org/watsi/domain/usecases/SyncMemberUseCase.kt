package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.MemberRepository

class SyncMemberUseCase(
    private val memberRepository: MemberRepository,
    private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedMemberDeltas = deltaRepository.unsynced(Delta.ModelName.MEMBER).blockingGet()

            unsyncedMemberDeltas.groupBy { it.modelId }.values.map { groupedDeltas ->
                Completable.concatArray(
                    memberRepository.sync(groupedDeltas),
                    deltaRepository.markAsSynced(groupedDeltas)
                ).onErrorComplete {
                    onError(it)
                }.blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}
