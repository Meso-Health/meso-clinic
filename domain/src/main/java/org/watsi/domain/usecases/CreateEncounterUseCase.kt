package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
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

    fun execute(encounterWithItemsAndForms: EncounterWithItemsAndForms): Completable {
        return Completable.fromAction {
            val encounterDeltas = mutableListOf<Delta>()
            val newBillables = mutableListOf<Billable>()
            val newPriceSchedules = mutableListOf<PriceSchedule>()

            encounterDeltas.add(Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.ENCOUNTER,
                    modelId = encounterWithItemsAndForms.encounter.id))

            encounterWithItemsAndForms.encounterForms.forEach { encounterForm ->
                encounterDeltas.add(Delta(
                        action = Delta.Action.ADD,
                        modelName = Delta.ModelName.ENCOUNTER_FORM,
                        modelId = encounterForm.id))
            }

            encounterWithItemsAndForms.encounterItemRelations.forEach { encounterItemRelation ->
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

            newBillables.forEach { billable ->
                val billableDelta = Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.BILLABLE,
                    modelId = billable.id
                )
                billableRepository.create(billable, billableDelta).blockingAwait()
            }

            newPriceSchedules.forEach { priceSchedule ->
                val priceScheduleDelta = Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.PRICE_SCHEDULE,
                    modelId = priceSchedule.id
                )
                priceScheduleRepository.create(priceSchedule, priceScheduleDelta).blockingAwait()
            }

            encounterRepository.insert(encounterWithItemsAndForms, encounterDeltas).blockingAwait()
        }.subscribeOn(Schedulers.io())
    }
}
