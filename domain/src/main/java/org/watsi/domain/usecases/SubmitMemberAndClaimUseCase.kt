package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterRepository

class SubmitMemberAndClaimUseCase(
    private val deltaRepository: DeltaRepository,
    private val encounterRepository: EncounterRepository
) {

    fun execute(member: Member, encounterWithItemsAndForms: EncounterWithItemsAndForms, clock: Clock): Completable {
        return Completable.fromAction {
            updateEncounterTimestamp(encounterWithItemsAndForms, clock)
            createDeltas(member, encounterWithItemsAndForms)
        }.subscribeOn(Schedulers.io())
    }

    private fun updateEncounterTimestamp(
        encounterWithItemsAndForms: EncounterWithItemsAndForms,
        clock: Clock
    ) {
        val encounterWithItemsAndFormsAndTimestamps =
            encounterWithItemsAndForms.copy(
                encounter = encounterWithItemsAndForms.encounter.copy(submittedAt = Instant.now(clock))
            )
        encounterRepository.update(listOf(encounterWithItemsAndFormsAndTimestamps.encounter))
    }

    private fun createDeltas(
        member: Member,
        encounterWithItemsAndForms: EncounterWithItemsAndForms
    ) {
        val deltas = mutableListOf<Delta>()

        deltas.add(Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.MEMBER,
            modelId = member.id))

        deltas.add(Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = encounterWithItemsAndForms.encounter.id))

        encounterWithItemsAndForms.encounterForms.forEach { encounterForm ->
            deltas.add(Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER_FORM,
                modelId = encounterForm.id))
        }

        encounterWithItemsAndForms.encounterItemRelations.forEach { encounterItemRelation ->
            if (encounterItemRelation.encounterItem.priceScheduleIssued) {
                deltas.add(Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.PRICE_SCHEDULE,
                    modelId = encounterItemRelation.billableWithPriceSchedule.priceSchedule.id
                ))
            }
        }

        deltaRepository.insert(deltas)
    }
}
