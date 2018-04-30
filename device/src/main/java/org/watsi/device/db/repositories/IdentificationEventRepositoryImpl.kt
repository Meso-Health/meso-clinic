package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.db.daos.IdentificationEventDao
import org.watsi.device.db.models.IdentificationEventModel
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.repositories.IdentificationEventRepository
import java.util.UUID

class IdentificationEventRepositoryImpl(private val identificationEventDao: IdentificationEventDao,
                                        private val clock: Clock) : IdentificationEventRepository {

    override fun create(identificationEvent: IdentificationEvent): Completable {
        return Completable.fromAction {
            identificationEventDao.insert(
                    IdentificationEventModel.fromIdentificationEvent(identificationEvent, clock))
        }.subscribeOn(Schedulers.io())
    }

    override fun dismiss(identificationEvent: IdentificationEvent): Completable {
        return Completable.fromAction {
            identificationEventDao.update(IdentificationEventModel.fromIdentificationEvent(
                    identificationEvent.copy(dismissed = true), clock))
        }.subscribeOn(Schedulers.io())
    }

    override fun openCheckIn(memberId: UUID): Maybe<IdentificationEvent> {
        return identificationEventDao.openCheckIn(memberId)
                .map { it.toIdentificationEvent() }
                .subscribeOn(Schedulers.io())
    }

    override fun sync(deltas: List<Delta>): Completable {
        // TODO: implement
        return Completable.complete()
    }
}
