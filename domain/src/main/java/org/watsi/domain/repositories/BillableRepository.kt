package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.BillableWithPriceSchedule
import java.util.UUID

interface BillableRepository {
    fun all(): Single<List<BillableWithPriceSchedule>>
    fun count(): Single<Int>
    fun ofType(type: Billable.Type): Single<List<BillableWithPriceSchedule>>
    fun find(id: UUID): Maybe<Billable>
    fun create(billable: Billable, delta: Delta): Completable
    fun delete(ids: List<UUID>): Completable
    fun fetch(): Completable
    fun opdDefaults(): Single<List<BillableWithPriceSchedule>>
    fun uniqueCompositions(): Single<List<String>>
    fun sync(delta: Delta): Completable
}
