package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.EncounterApi
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.EncounterRepository
import java.util.UUID

class EncounterRepositoryImpl(private val encounterDao: EncounterDao,
                              private val api: CoverageApi,
                              private val sessionManager: SessionManager,
                              private val clock: Clock) : EncounterRepository {

    override fun find(id: UUID): Single<Encounter> {
        return encounterDao.find(id).map { it.toEncounter() }.subscribeOn(Schedulers.io())
    }

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
        val authToken = sessionManager.currentToken()!!

        // TODO: create EncounterWithItems relation entity to avoid extra query
        return encounterDao.find(deltas.first().modelId).flatMapCompletable { encounterModel ->
            val encounter = encounterModel.toEncounter()
            encounterDao.findEncounterItems(encounter.id).flatMapCompletable { encounterItemModels ->
                api.postEncounter(authToken.getHeaderString(), authToken.user.providerId,
                        EncounterApi(encounter, encounterItemModels.map { it.toEncounterItem() })
                )
            }
        }.subscribeOn(Schedulers.io())
    }
}
