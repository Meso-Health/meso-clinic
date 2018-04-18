package org.watsi.uhp.repositories

import org.watsi.uhp.models.Billable
import java.util.UUID

interface BillableRepository {
    fun find(id: UUID): Billable?
    fun createOrUpdate(billable: Billable)
    fun clearBillablesWithoutUnsyncedEncounter()
    fun findByName(name: String): Billable
    fun findByType(type: Billable.TypeEnum): List<Billable>
    fun uniqueDrugNames(): Set<String>
    fun uniqueCompositions(): Set<String>
    fun fuzzySearchDrugsByName(query: String): List<Billable>
}
