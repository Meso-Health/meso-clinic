package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithItemsAndForms
import java.util.UUID

class ReviseMemberAndClaimUseCase(
    private val createMemberUseCase: CreateMemberUseCase,
    private val createEncounterUseCase: CreateEncounterUseCase,
    private val markReturnedEncounterAsRevisedUseCase: MarkReturnedEncountersAsRevisedUseCase
) {

    fun execute(member: Member, encounterWithItemsAndForms: EncounterWithItemsAndForms): Completable {
        return Completable.fromAction {

            val newMember = member.copy(id = UUID.randomUUID())
            createMemberUseCase.execute(newMember).blockingAwait()

            val newEncounter = encounterWithItemsAndForms.encounter.copy(
                id = UUID.randomUUID(),
                memberId = newMember.id,
                revisedEncounterId = encounterWithItemsAndForms.encounter.id,
                adjudicationState = Encounter.AdjudicationState.PENDING,
                adjudicatedAt = null,
                returnReason = null
            )

            val newEncounterItems = mutableListOf<EncounterItemWithBillableAndPrice>()
            encounterWithItemsAndForms.encounterItemRelations.forEach {
                newEncounterItems.add(
                    it.copy(
                        encounterItem = it.encounterItem.copy(
                            id = UUID.randomUUID(),
                            encounterId = newEncounter.id
                        )
                    )
                )
            }

            val newEncounterForms = mutableListOf<EncounterForm>()
            encounterWithItemsAndForms.encounterForms.forEach {
                newEncounterForms.add(it.copy(
                    id = UUID.randomUUID(),
                    encounterId = newEncounter.id
                ))
            }

            createEncounterUseCase.execute(
                EncounterWithItemsAndForms(
                    newEncounter,
                    newEncounterItems,
                    newEncounterForms,
                    encounterWithItemsAndForms.diagnoses
                )
            ).blockingAwait()

            markReturnedEncounterAsRevisedUseCase.execute(listOf(encounterWithItemsAndForms.encounter.id))
                .blockingAwait()
        }.subscribeOn(Schedulers.io())
    }
}
