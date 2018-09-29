package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.relations.EncounterWithItems
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.EncounterFormRepository
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

class DeletePendingClaimAndMemberUseCase(
    private val encounterRepository: EncounterRepository
) {
    fun execute(encounterWithExtras: EncounterWithExtras): Completable {
        return encounterRepository.delete(encounterWithExtras).subscribeOn(Schedulers.io())
    }
}
