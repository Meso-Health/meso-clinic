package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.PriceSchedule
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.PriceScheduleRepository

class UpdateEncounterUseCase(
    private val encounterRepository: EncounterRepository,
    private val priceScheduleRepository: PriceScheduleRepository
) {

    fun execute(encounterWithItemsAndForms: EncounterWithItemsAndForms): Completable {
        return Completable.fromAction {
            val newPriceSchedules = encounterWithItemsAndForms.encounterItemRelations
                    .map { it.billableWithPriceSchedule.priceSchedule }
                    .filter { priceScheduleRepository.find(it.id).blockingGet() == null }

            createPriceSchedules(newPriceSchedules)
            // upsert so that newly added encounterItems are saved to the DB
            encounterRepository.upsert(encounterWithItemsAndForms).blockingAwait()
        }.subscribeOn(Schedulers.io())
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
}
