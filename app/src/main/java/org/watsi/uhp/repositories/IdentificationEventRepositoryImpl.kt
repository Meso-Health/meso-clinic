package org.watsi.uhp.repositories

import org.watsi.uhp.database.IdentificationEventDao
import org.watsi.uhp.models.IdentificationEvent
import java.util.UUID

class IdentificationEventRepositoryImpl : IdentificationEventRepository {

    override fun openCheckIn(memberId: UUID): IdentificationEvent? {
        return IdentificationEventDao.openCheckIn(memberId)
    }
}
