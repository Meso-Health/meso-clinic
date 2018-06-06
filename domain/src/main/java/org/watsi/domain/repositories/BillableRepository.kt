package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Delta
import java.util.UUID

interface BillableRepository {
    fun all(): Single<List<Billable>>
    fun find(id: UUID): Maybe<Billable>
    fun create(billable: Billable, delta: Delta): Completable
    fun fetch(): Completable
    fun opdDefaults(): Single<List<Billable>>
    fun uniqueCompositions(): Single<List<String>>
    fun sync(delta: Delta): Completable
}
