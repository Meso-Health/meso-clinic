package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.IdentificationEventApi
import org.watsi.device.db.daos.IdentificationEventDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.IdentificationEventModel
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.repositories.IdentificationEventRepository
import java.util.UUID

class IdentificationEventRepositoryImpl(
        private val identificationEventDao: IdentificationEventDao,
        private val api: CoverageApi,
        private val sessionManager: SessionManager,
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
