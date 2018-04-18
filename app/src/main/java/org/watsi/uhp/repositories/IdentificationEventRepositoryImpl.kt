package org.watsi.uhp.repositories

import org.watsi.domain.entities.Delta
import org.watsi.uhp.database.DatabaseHelper
import org.watsi.uhp.database.IdentificationEventDao
import org.watsi.uhp.models.Encounter
import org.watsi.uhp.models.IdentificationEvent
import java.util.UUID

class IdentificationEventRepositoryImpl : IdentificationEventRepository {

    override fun create(identificationEvent: IdentificationEvent) {
        // TODO: set token, validate and set ID (if necessary)
        DatabaseHelper.fetchDao(Encounter::class.java).create(identificationEvent)
    }

    override fun update(identificationEvent: IdentificationEvent) {
        DatabaseHelper.fetchDao(Encounter::class.java).update(identificationEvent)
    }

    override fun openCheckIn(memberId: UUID): IdentificationEvent? {
        return IdentificationEventDao.openCheckIn(memberId)
    }

    override fun sync(deltas: List<Delta>) {
        // TODO
    }
}
