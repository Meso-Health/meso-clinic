package org.watsi.uhp.repositories

import org.watsi.uhp.database.BillableDao
import org.watsi.uhp.database.DatabaseHelper
import org.watsi.uhp.models.Billable
import java.util.UUID

class BillableRepositoryImpl : BillableRepository {

    override fun find(id: UUID): Billable? {
        return DatabaseHelper.fetchDao(Billable::class.java).queryForId(id) as Billable?
    }

    override fun createOrUpdate(billable: Billable) {
        // TODO: set token, validate, set ID (if necessary)
        DatabaseHelper.fetchDao(Billable::class.java).createOrUpdate(billable)
    }

    override fun clearBillablesWithoutUnsyncedEncounter() {
        BillableDao.clearBillablesWithoutUnsyncedEncounter()
    }

    override fun findByName(name: String): Billable {
        return BillableDao.findBillableByName(name)
    }

    override fun uniqueDrugNames(): Set<String> {
        return BillableDao.allUniqueDrugNames()
    }

    override fun fuzzySearchDrugsByName(query: String): List<Billable> {
        return BillableDao.fuzzySearchDrugs(query)
    }

    override fun findByType(type: Billable.TypeEnum): List<Billable> {
        return BillableDao.getBillablesByType(type)
    }

    override fun uniqueCompositions(): Set<String> {
        return BillableDao.getUniqueBillableCompositions().toSet()
    }
}
