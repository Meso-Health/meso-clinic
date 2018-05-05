package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel
import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.EncounterRepository

class EncounterRepositoryImpl(private val encounterDao: EncounterDao,
                              private val clock: Clock) : EncounterRepository {

    override fun create(encounterWithItemsAndForms: EncounterWithItemsAndForms,
                        deltas: List<Delta>): Completable {
        val encounterModel = EncounterModel.fromEncounter(encounterWithItemsAndForms.encounter, clock)
        val encounterItemModels = encounterWithItemsAndForms.encounterItems.map {
            EncounterItemModel.fromEncounterItem(it.encounterItem, clock)
        }
        // TODO: select any billables that need to be inserted
        val encounterFormModels = encounterWithItemsAndForms.encounterForms.map {
            EncounterFormModel.fromEncounterForm(it, clock)
        }

        return Completable.fromAction {
            encounterDao.insert(encounterModel,
                                encounterItemModels,
                                emptyList(),
                                encounterFormModels,
                                deltas.map { DeltaModel.fromDelta(it, clock) })
        }.subscribeOn(Schedulers.io())
    }

    override fun sync(deltas: List<Delta>): Completable {
        // TODO: implement
        return Completable.complete()
    }
}
