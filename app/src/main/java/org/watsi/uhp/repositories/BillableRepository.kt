package org.watsi.uhp.repositories

import org.watsi.uhp.models.Billable

interface BillableRepository {
    fun createOrUpdate(billable: Billable)
    fun clearBillablesWithoutUnsyncedEncounter()
    fun findByName(name: String): Billable
    fun findByType(type: Billable.TypeEnum): List<Billable>
    fun uniqueDrugNames(): Set<String>
    fun uniqueCompositions(): Set<String>
    fun fuzzySearchByName(query: String): List<Billable>
}
