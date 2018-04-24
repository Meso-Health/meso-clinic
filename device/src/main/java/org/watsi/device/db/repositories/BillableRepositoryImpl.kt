package org.watsi.device.db.repositories

import me.xdrop.fuzzywuzzy.FuzzySearch
import org.threeten.bp.Clock
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.models.BillableModel
import org.watsi.domain.entities.Billable
import org.watsi.domain.repositories.BillableRepository
import java.util.UUID

class BillableRepositoryImpl(private val billableDao: BillableDao,
                             private val clock: Clock) : BillableRepository {

    override fun find(id: UUID): Billable {
        return billableDao.find(id).toBillable()
    }

    override fun createOrUpdate(billable: Billable) {
        // TODO: need to handle the "orUpdate" part
        billableDao.insert(BillableModel.fromBillable(billable, clock))
    }

    override fun clearBillablesWithoutUnsyncedEncounter() {
        // TODO: implement
    }

    override fun findByName(name: String): List<Billable> {
        return billableDao.findByName(name).map { it.toBillable() }
    }

    override fun findByType(type: Billable.Type): List<Billable> {
        return billableDao.findByType(type).map { it.toBillable() }
    }

    override fun uniqueCompositions(): Set<String> {
        return billableDao.distinctCompositions().toSet()
    }

    override fun fuzzySearchDrugsByName(query: String): List<Billable> {
        val uniqueDrugNames = billableDao.distinctDrugNames()

        val topMatchingNames = FuzzySearch.extractTop(query, uniqueDrugNames, 5, 50)

        return topMatchingNames.map { result ->
            findByName(result.string).filter { it.type == Billable.Type.DRUG }
        }.flatten()
    }

    override fun fetch() {
        // TODO implement
    }
}
