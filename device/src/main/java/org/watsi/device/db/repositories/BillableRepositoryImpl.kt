package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.BillableApi
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.BillableRepository
import java.util.UUID

class BillableRepositoryImpl(
        private val billableDao: BillableDao,
        private val api: CoverageApi,
        private val sessionManager: SessionManager,
        private val preferencesManager: PreferencesManager,
        private val clock: Clock
) : BillableRepository {

    override fun all(): Single<List<Billable>> {
        return billableDao.all().map { it.map { it.toBillable() } }.subscribeOn(Schedulers.io())
    }

    override fun find(id: UUID): Maybe<Billable> {
        return billableDao.find(id).map { it.toBillable() }.subscribeOn(Schedulers.io())
    }

    override fun create(billable: Billable, delta: Delta): Completable {
        return Completable.fromAction {
            billableDao.insertWithDelta(
                    BillableModel.fromBillable(billable, clock),
                    DeltaModel.fromDelta(delta, clock)
            )
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Removes any synced persisted billables that are not returned in the API results and
     * overwrites any persisted data if the API response contains updated data
     */
    override fun fetch(): Completable {
        return sessionManager.currentToken()?.let { token ->
            Completable.fromAction {
                val fetchedBillables = api.getBillables(token.getHeaderString(), token.user.providerId).blockingGet()
                val fetchedBillableIds = fetchedBillables.map { it.id }
                val persistedBillableIds = billableDao.all().blockingGet().map { it.id }
                val unsyncedBillableIds = billableDao.unsynced().blockingGet().map { it.id }
                val syncedBillableIds = persistedBillableIds.minus(unsyncedBillableIds)
                val deletedBillableIds = syncedBillableIds.minus(fetchedBillableIds)

                billableDao.delete(deletedBillableIds)
                billableDao.upsert(fetchedBillables.map { billableApi ->
                    BillableModel.fromBillable(billableApi.toBillable(), clock)
                })
                preferencesManager.updateBillablesLastFetched(clock.instant())
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }

    override fun uniqueCompositions(): Single<List<String>> {
        return billableDao.distinctCompositions().subscribeOn(Schedulers.io())
    }

    override fun opdDefaults(): Single<List<Billable>> {
        return billableDao.opdDefaults().map { it.map { it.toBillable() } }.subscribeOn(Schedulers.io())
    }

    override fun sync(delta: Delta): Completable {
        return sessionManager.currentToken()?.let { token ->
            billableDao.find(delta.modelId).flatMapCompletable { billableModel ->
                val billable = billableModel.toBillable()
                api.postBillable(token.getHeaderString(), token.user.providerId, BillableApi(billable))
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }
}
