package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.PriceSchedule
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.PriceScheduleRepository

class CreateEncounterUseCase(
    private val encounterRepository: EncounterRepository,
    private val billableRepository: BillableRepository,
    private val priceScheduleRepository: PriceScheduleRepository
) {

    fun execute(encounterWithItemsAndForms: EncounterWithItemsAndForms, submitNow: Boolean, clock: Clock): Completable {
        return Completable.fromAction {

            val newBillables = mutableListOf<Billable>()
            val newPriceSchedules = mutableListOf<PriceSchedule>()

            val encounterWithItemsAndFormsAndTimestamps =
                addEncounterTimeStamps(encounterWithItemsAndForms, submitNow, clock)

            encounterWithItemsAndFormsAndTimestamps.encounterItemRelations.forEach { encounterItemRelation ->
                val billableWithPrice = encounterItemRelation.billableWithPriceSchedule
                if (billableRepository.find(billableWithPrice.billable.id).blockingGet() == null) {
                    newBillables.add(billableWithPrice.billable)
                    newPriceSchedules.add(billableWithPrice.priceSchedule) // A new billable always creates a new PriceSchedule
                } else if (priceScheduleRepository.find(billableWithPrice.priceSchedule.id).blockingGet() == null) {
                    newPriceSchedules.add(billableWithPrice.priceSchedule) // An existing billable may still have a new PriceSchedule
                } else {
                    // Billable and PriceSchedule both exist -> No Op
                }
            }

            createBillables(newBillables)
            createPriceSchedules(newPriceSchedules)
            createEncounter(encounterWithItemsAndFormsAndTimestamps, submitNow)
        }.subscribeOn(Schedulers.io())
    }

    private fun addEncounterTimeStamps(
        encounterWithItemsAndForms: EncounterWithItemsAndForms,
        submitNow: Boolean,
        clock: Clock
    ): EncounterWithItemsAndForms {
        val preparedAt = Instant.now(clock)
        val submittedAt =  if (submitNow) preparedAt else null
        return encounterWithItemsAndForms.copy(
            encounter = encounterWithItemsAndForms.encounter.copy(
                preparedAt = preparedAt,
                submittedAt = submittedAt
            )
        )
    }

    private fun createBillables(newBillables: List<Billable>) {
        newBillables.forEach { billable ->
            val billableDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.BILLABLE,
                modelId = billable.id
            )
            billableRepository.create(billable, billableDelta).blockingAwait()
        }
    }

    // Always create price schedule with Delta for immediate syncing since we are never deleting
    // price schedules (see https://watsi.slack.com/archives/C03T9TUT1/p1538150314000100)
    private fun createPriceSchedules(newPriceSchedules: List<PriceSchedule>) {
        newPriceSchedules.forEach { priceSchedule ->
            val priceScheduleDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.PRICE_SCHEDULE,
                modelId = priceSchedule.id
            )
            priceScheduleRepository.create(priceSchedule, priceScheduleDelta).blockingAwait()
        }
    }

    private fun createEncounter(
        encounterWithItemsAndForms: EncounterWithItemsAndForms,
        submitNow: Boolean
    ) {
        val deltas = mutableListOf<Delta>()

        if (submitNow) {
            deltas.add(
                Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.ENCOUNTER,
                    modelId = encounterWithItemsAndForms.encounter.id
                )
            )

            encounterWithItemsAndForms.encounterForms.forEach { encounterForm ->
                deltas.add(
                    Delta(
                        action = Delta.Action.ADD,
                        modelName = Delta.ModelName.ENCOUNTER_FORM,
                        modelId = encounterForm.id
                    )
                )
            }
        }

        encounterRepository.insert(encounterWithItemsAndForms, deltas).blockingAwait()
    }

}
