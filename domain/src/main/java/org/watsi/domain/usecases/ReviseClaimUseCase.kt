package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithExtras
import java.util.UUID

class ReviseClaimUseCase(
    private val createEncounterUseCase: CreateEncounterUseCase,
    private val markReturnedEncounterAsRevisedUseCase: MarkReturnedEncountersAsRevisedUseCase
) {

    fun execute(originalEncounterWithExtras: EncounterWithExtras, clock: Clock): Completable {
        return Completable.fromAction {
            val newEncounter = originalEncounterWithExtras.encounter.copy(
                id = UUID.randomUUID(),
                revisedEncounterId = originalEncounterWithExtras.encounter.id,
                adjudicationState = Encounter.AdjudicationState.PENDING,
                adjudicatedAt = null,
                adjudicationReason = null
            )

            val newEncounterItems = mutableListOf<EncounterItemWithBillableAndPrice>()
            originalEncounterWithExtras.encounterItemRelations.forEach {
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
            originalEncounterWithExtras.encounterForms.forEach {
                newEncounterForms.add(it.copy(
                    id = UUID.randomUUID(),
                    encounterId = newEncounter.id
                ))
            }

            val newReferral = originalEncounterWithExtras.referral?.copy(
                id = UUID.randomUUID(),
                encounterId = newEncounter.id
            )

            createEncounterUseCase.execute(
                encounterWithExtras = EncounterWithExtras(
                    encounter = newEncounter,
                    encounterItemRelations = newEncounterItems,
                    encounterForms = newEncounterForms,
                    referral = newReferral,
                    member = originalEncounterWithExtras.member,
                    diagnoses = originalEncounterWithExtras.diagnoses
                ),
                submitNow = true,
                isPartial = false,
                clock = clock
            ).blockingAwait()

            markReturnedEncounterAsRevisedUseCase.execute(listOf(originalEncounterWithExtras.encounter.id)).blockingAwait()
        }.subscribeOn(Schedulers.io())
    }
}
