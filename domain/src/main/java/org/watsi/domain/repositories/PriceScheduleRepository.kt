package org.watsi.domain.repositories

import io.reactivex.Completable
import org.watsi.domain.entities.Delta

interface PriceScheduleRepository {
    fun sync(delta: Delta): Completable
}
