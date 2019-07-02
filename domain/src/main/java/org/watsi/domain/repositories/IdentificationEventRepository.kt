package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.IdentificationEvent
import java.util.UUID

interface IdentificationEventRepository {
    fun create(identificationEvent: IdentificationEvent, delta: Delta): Completable
    fun dismiss(identificationEvent: IdentificationEvent): Completable
    fun openCheckIn(memberId: UUID): Single<IdentificationEvent>
    fun fetch(): Completable
    fun sync(deltas: List<Delta>): Completable
}
