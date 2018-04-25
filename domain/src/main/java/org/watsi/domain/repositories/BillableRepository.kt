package org.watsi.domain.repositories

import org.watsi.domain.entities.Billable
import java.util.UUID

interface BillableRepository {
    fun find(id: UUID): Billable
    fun create(billable: Billable)
    fun fetch()
    fun findByName(name: String): List<Billable>
    fun findByType(type: Billable.Type): List<Billable>
    fun uniqueCompositions(): Set<String>
    fun fuzzySearchDrugsByName(query: String): List<Billable>
}
