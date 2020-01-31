package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.PriceSchedule
import java.util.UUID

interface PriceScheduleRepository {
    fun find(id: UUID): Maybe<PriceSchedule>
    fun create(priceSchedule: PriceSchedule, delta: Delta?): Completable
    fun sync(delta: Delta): Completable
}
