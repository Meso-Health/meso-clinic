package org.watsi.domain.repositories

import org.watsi.domain.entities.Billable
import java.util.UUID

interface BillableRepository {
    fun find(id: UUID): Billable
    fun all(): List<Billable>
    fun create(billable: Billable)
    fun fetch()
    fun uniqueCompositions(): List<String>
}
