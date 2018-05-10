package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Billable
import org.watsi.domain.repositories.BillableRepository

class BillableRepositoryImpl(private val billableDao: BillableDao,
                             private val api: CoverageApi,
                             private val sessionManager: SessionManager,
                             private val preferencesManager: PreferencesManager,
                             private val clock: Clock) : BillableRepository {

    override fun all(): Single<List<Billable>> {
        return billableDao.all().map { it.map { it.toBillable() } }.subscribeOn(Schedulers.io())
    }

    override fun create(billable: Billable): Completable {
        return Completable.fromAction {
            billableDao.insert(BillableModel.fromBillable(billable, clock))
        }.subscribeOn(Schedulers.io())
    }

    override fun fetch(): Completable {
        return sessionManager.currentToken()?.let { token ->
            api.billables(token.getHeaderString(),
                          token.user.providerId).flatMapCompletable { fetchedBillables ->
                billableDao.unsynced().flatMapCompletable {
                    Completable.fromAction {
                        val fetchedAndUnsyncedIds = fetchedBillables.map { it.id } + it.map { it.id }
                        billableDao.deleteNotInList(fetchedAndUnsyncedIds)
                        billableDao.insert(fetchedBillables.map { billableApi ->
                            BillableModel.fromBillable(billableApi.toBillable(), clock)
                        })
                        preferencesManager.updateBillablesLastFetched(clock.instant())
                    }
                }
            }.subscribeOn(Schedulers.io())
        } ?: Completable.complete()
    }

    override fun uniqueCompositions(): Single<List<String>> {
        return billableDao.distinctCompositions().subscribeOn(Schedulers.io())
    }
}
