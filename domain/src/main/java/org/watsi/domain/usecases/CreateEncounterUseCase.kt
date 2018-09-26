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

            createBillables(newBillables, submitNow)
            createPriceSchedules(newPriceSchedules, submitNow)
            createEncounter(encounterWithItemsAndForms, submitNow)
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

    private fun createBillables(
        newBillables: List<Billable>,
        submitNow: Boolean
    ) {
        newBillables.forEach { billable ->
            var billableDelta: Delta? = null
            if (submitNow) {
                // TODO: Currently, if a billable's Deltas are not created here, they will NEVER be created.
                // This is because we have no way of knowing which billables are new after they are persisted.
                // This works for now because Ethiopia is the only case where we delay creating Deltas and they
                // don't create billables
                billableDelta = Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.BILLABLE,
                    modelId = billable.id
                )
            }
            billableRepository.create(billable, billableDelta).blockingAwait()
        }
    }

    private fun createPriceSchedules(
        newPriceSchedules: List<PriceSchedule>,
        submitNow: Boolean
    ) {
        newPriceSchedules.forEach { priceSchedule ->
            var priceScheduleDelta: Delta? = null
            if (submitNow) {
                priceScheduleDelta = Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.PRICE_SCHEDULE,
                    modelId = priceSchedule.id
                )
            }
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
