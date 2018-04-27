package org.watsi.device.db.repositories

import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Billable
import org.watsi.domain.repositories.BillableRepository
import java.util.UUID

class BillableRepositoryImpl(private val billableDao: BillableDao,
                             private val api: CoverageApi,
                             private val sessionManager: SessionManager,
                             private val preferencesManager: PreferencesManager,
                             private val clock: Clock) : BillableRepository {

    override fun find(id: UUID): Billable {
        return billableDao.find(id)!!.toBillable()
    }

    override fun all(): List<Billable> {
        return billableDao.all().map { it.toBillable() }
    }

    override fun create(billable: Billable) {
        billableDao.insert(BillableModel.fromBillable(billable, clock))
    }

    private fun save(billable: Billable) {
        if (billableDao.find(billable.id) != null) {
            billableDao.update(BillableModel.fromBillable(billable, clock))
        } else {
            billableDao.insert(BillableModel.fromBillable(billable, clock))
        }
    }

    override fun fetch() {
        sessionManager.currentToken()?.let { token ->
            api.billables(token.getHeaderString(), token.user.providerId).execute()?.let { response ->
                // TODO: handle null body
                if (response.isSuccessful) {
                    response.body()?.let { billables ->
                        billables.forEach { save(it.toBillable()) }
                        // TODO: more efficient way of saving?
                        // TODO: clean up any billables not returned in the fetch
                        // TODO: do not overwrite unsynced billable data
                    }
                    preferencesManager.updateBillablesLastFetched(clock.instant())
                } else {
                    // TODO: log
                }
                // TODO: handle null response

            }
        }
    }

    override fun uniqueCompositions(): List<String> {
        return billableDao.distinctCompositions()
    }
}
