package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.BillableApi
import org.watsi.device.db.DbHelper
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.PriceScheduleModel
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

    override fun ofType(type: Billable.Type): Single<List<Billable>> {
        return billableDao.ofType(type).map { it.map { it.toBillable() } }.subscribeOn(Schedulers.io())
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

    override fun delete(ids: List<UUID>): Completable {
        return Completable.fromAction {
            ids.chunked(DbHelper.SQLITE_MAX_VARIABLE_NUMBER).map { billableDao.delete(it) }
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Removes any synced persisted billables that are not returned in the API results and
     * overwrites any persisted data if the API response contains updated data. Do not
     * remove or overwrite any unsynced data (new billables).
     */
    override fun fetch(): Completable {
        return sessionManager.currentToken()?.let { token ->
            Completable.fromCallable {
                val serverBillablesWithPrice = api.getBillables(token.getHeaderString(), token.user.providerId).blockingGet()
                        .map { it.toBillableWithPriceSchedule() }
                val serverBillableIds = serverBillablesWithPrice.map { it.billable.id }
                val clientBillableIds = billableDao.all().blockingGet().map { it.id }
                val unsyncedClientBillableIds = billableDao.unsynced().blockingGet().map { it.id }
                val syncedClientBillableIds = clientBillableIds.minus(unsyncedClientBillableIds)
                val serverRemovedBillableIds = syncedClientBillableIds.minus(serverBillableIds)

                billableDao.upsert(
                    serverBillablesWithPrice.map { BillableModel.fromBillable(it.billable, clock) },
                    serverBillablesWithPrice.map { PriceScheduleModel.fromPriceSchedule(it.priceSchedule, clock) }
                )

                billableDao.delete(serverRemovedBillableIds)
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
