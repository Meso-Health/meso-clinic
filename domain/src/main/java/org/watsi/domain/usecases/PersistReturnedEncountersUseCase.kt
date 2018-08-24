package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.MemberRepository

class PersistReturnedEncountersUseCase(
    private val encounterRepository: EncounterRepository,
    private val memberRepository: MemberRepository
) {
    fun execute(encounters: List<EncounterWithExtras>): Completable {
        return Completable.fromCallable {
            val revisedEncounterIds = encounterRepository.revisedIds().blockingGet()
            encounters.filter {
                !revisedEncounterIds.contains(it.encounter.id)
            }.forEach { encounterWithExtras ->
                val member = encounterWithExtras.member
                memberRepository.upsert(member, deltas = emptyList()).blockingAwait()
                encounterRepository.upsert(encounterWithExtras.toEncounterWithItemsAndForms()).blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}
