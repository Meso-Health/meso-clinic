package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithItemsAndForms
import java.util.UUID

class ReviseClaimUseCase(
    private val createEncounterUseCase: CreateEncounterUseCase,
    private val markReturnedEncounterAsRevisedUseCase: MarkReturnedEncountersAsRevisedUseCase
) {

    fun execute(encounterWithItemsAndForms: EncounterWithItemsAndForms, clock: Clock): Completable {
        return Completable.fromAction {
            val newEncounter = encounterWithItemsAndForms.encounter.copy(
                id = UUID.randomUUID(),
                revisedEncounterId = encounterWithItemsAndForms.encounter.id,
                adjudicationState = Encounter.AdjudicationState.PENDING,
                adjudicatedAt = null,
                adjudicationReason = null
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

            val newReferral = encounterWithItemsAndForms.referral?.copy(
                id = UUID.randomUUID(),
                encounterId = newEncounter.id
            )

            createEncounterUseCase.execute(
                EncounterWithItemsAndForms(
                    encounter = newEncounter,
                    encounterItemRelations = newEncounterItems,
                    encounterForms = newEncounterForms,
                    referral = newReferral
                ), true, clock
            ).blockingAwait()

            markReturnedEncounterAsRevisedUseCase.execute(listOf(encounterWithItemsAndForms.encounter.id))
                .blockingAwait()
        }.subscribeOn(Schedulers.io())
    }
}
