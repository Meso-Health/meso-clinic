package org.watsi.uhp.repositories

import org.watsi.uhp.database.BillableDao
import org.watsi.uhp.models.Billable

class BillableRepositoryImpl : BillableRepository {
    override fun createOrUpdate(billable: Billable) {
        billable.createOrUpdate()
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

    override fun fuzzySearchByName(query: String): List<Billable> {
        return BillableDao.fuzzySearchDrugs(query)
    }

    override fun findByType(type: Billable.TypeEnum): List<Billable> {
        return BillableDao.getBillablesByType(type)
    }

    override fun uniqueCompositions(): Set<String> {
        return BillableDao.getUniqueBillableCompositions().toSet()
    }
}
