package org.watsi.device.db.repositories

import io.reactivex.Single
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

    override fun create(identificationEvent: IdentificationEvent) {
        identificationEventDao.insert(
                IdentificationEventModel.fromIdentificationEvent(identificationEvent, clock))
    }

    override fun update(identificationEvent: IdentificationEvent) {
        identificationEventDao.update(
                IdentificationEventModel.fromIdentificationEvent(identificationEvent, clock))
    }

    override fun openCheckIn(memberId: UUID): Single<IdentificationEvent?> {
        return identificationEventDao.openCheckIn(memberId).map { it.toIdentificationEvent() }
                .subscribeOn(Schedulers.io())
    }

    override fun sync(deltas: List<Delta>) {
        // TODO: implement
    }
}
