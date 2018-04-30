package org.watsi.domain.repositories

import io.reactivex.Single
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.IdentificationEvent
import java.util.UUID

interface IdentificationEventRepository {
    fun create(identificationEvent: IdentificationEvent)
    fun update(identificationEvent: IdentificationEvent)
    fun openCheckIn(memberId: UUID): Single<IdentificationEvent?>
    fun sync(deltas: List<Delta>)
}
