package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.IdentificationEventApi
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.daos.IdentificationEventDao
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.IdentificationEventModel
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.repositories.IdentificationEventRepository
import java.util.UUID

class IdentificationEventRepositoryImpl(
    private val identificationEventDao: IdentificationEventDao,
    private val encounterDao: EncounterDao,
    private val api: CoverageApi,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val clock: Clock
) : IdentificationEventRepository {

    override fun create(identificationEvent: IdentificationEvent, delta: Delta): Completable {
        return Completable.fromAction {
            identificationEventDao.insertWithDelta(
                    IdentificationEventModel.fromIdentificationEvent(identificationEvent, clock),
                    DeltaModel.fromDelta(delta, clock))
        }.subscribeOn(Schedulers.io())
    }

    override fun dismiss(identificationEvent: IdentificationEvent): Completable {
        return Completable.fromAction {
            val delta = Delta(
                action = Delta.Action.EDIT,
                modelName = Delta.ModelName.IDENTIFICATION_EVENT,
                modelId = identificationEvent.id,
                field = "dismissed"
            )
            identificationEventDao.upsertWithDelta(
                IdentificationEventModel.fromIdentificationEvent(identificationEvent.copy(dismissed = true), clock),
                DeltaModel.fromDelta(delta, clock)
            )
        }.subscribeOn(Schedulers.io())
    }

    override fun openCheckIn(memberId: UUID): Single<IdentificationEvent> {
        return identificationEventDao.openCheckIn(memberId)
                .map { it.toIdentificationEvent() }
                .subscribeOn(Schedulers.io())
    }

    /**
     * Removes any synced client identifications that are not returned in the API results and
     * overwrites any synced client identifications if the API response contains updated data. Does not
     * remove or overwrite any unsynced data (new or edited identifications).
     */
    override fun fetch(): Completable {
        return sessionManager.currentAuthenticationToken()?.let { token ->
            Completable.fromAction {
                // Fetch full open identification events list from the server
                val serverIdentificationEvents =
                    api.getOpenIdentificationEvents(token.getHeaderString(), token.user.providerId)
                        .blockingGet()
                val serverIdentificationEventIds = serverIdentificationEvents.map { it.id }

                // Get all locally stored identification events
                val clientIdentificationEvents = identificationEventDao.all().blockingFirst()
                val clientIdentificationEventIds = clientIdentificationEvents.map { it.id }
                val clientIdentificationEventsById = clientIdentificationEvents.groupBy { it.id }

                // Find all locally stored identification events that are unsynced or in use, so that we know not to delete them
                val identificationEventIdsToRetain: MutableList<UUID> = mutableListOf()
                identificationEventIdsToRetain.addAll(serverIdentificationEventIds)

                val unsyncedClientIdentificationEventIds = identificationEventDao.unsynced().blockingGet().map { it.id }
                identificationEventIdsToRetain.addAll(unsyncedClientIdentificationEventIds)

                val unsyncedEncounterIdentificationEventIds = encounterDao.unsynced().blockingGet().map { it.encounterModel?.identificationEventId }.requireNoNulls()
                identificationEventIdsToRetain.addAll(unsyncedEncounterIdentificationEventIds)

                // Delete stale identification events
                val identificationEventsToDelete = clientIdentificationEventIds.minus(identificationEventIdsToRetain)
                identificationEventDao.delete(identificationEventsToDelete)

                preferencesManager.updateIdentificationEventsLastFetched(clock.instant())
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }

    override fun sync(deltas: List<Delta>): Completable {
        return sessionManager.currentAuthenticationToken()?.let { token ->
            identificationEventDao.find(deltas.first().modelId).flatMapCompletable { identificationEventModel ->
                if (deltas.any { it.action == Delta.Action.ADD }) {
                    api.postIdentificationEvent(
                        tokenAuthorization = token.getHeaderString(),
                        providerId = token.user.providerId,
                        identificationEvent = IdentificationEventApi(identificationEventModel.toIdentificationEvent())
                    )
                } else {
                    api.patchIdentificationEvent(
                        tokenAuthorization = token.getHeaderString(),
                        identificationEventId = identificationEventModel.id,
                        patchParams = IdentificationEventApi.patch(identificationEventModel.toIdentificationEvent(), deltas)
                    )
                }
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }
}
