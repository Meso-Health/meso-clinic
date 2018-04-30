package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.domain.entities.Billable

interface BillableRepository {
    fun all(): Single<List<Billable>>
    fun create(billable: Billable): Completable
    fun fetch(): Completable
    fun uniqueCompositions(): Single<List<String>>
}
