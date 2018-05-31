package org.watsi.uhp.services

import android.app.job.JobParameters
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.watsi.device.managers.Logger
import org.watsi.domain.usecases.SyncBillableUseCase
import org.watsi.domain.usecases.SyncEncounterUseCase
import org.watsi.domain.usecases.SyncIdentificationEventUseCase
import org.watsi.domain.usecases.SyncMemberUseCase
import javax.inject.Inject

class SyncDataService : DaggerJobService() {

    @Inject lateinit var syncMemberUseCase: SyncMemberUseCase
    @Inject lateinit var syncIdentificationEventUseCase: SyncIdentificationEventUseCase
    @Inject lateinit var syncBillableUseCase: SyncBillableUseCase
    @Inject lateinit var syncEncounterUseCase: SyncEncounterUseCase
    @Inject lateinit var logger: Logger
    private lateinit var disposable: Disposable
    private var errors = mutableListOf<Throwable>()

    override fun onStartJob(params: JobParameters): Boolean {
        Completable.concatArray(
                syncMemberUseCase.execute().onErrorComplete { errors.add(it) },
                syncIdentificationEventUseCase.execute().onErrorComplete { errors.add(it) },
                syncBillableUseCase.execute().onErrorComplete { errors.add(it) },
                syncEncounterUseCase.execute().onErrorComplete { errors.add(it) },
                Completable.fromAction {
                    if (errors.size > 0) { throw Exception(errors.map { it.message }.joinToString()) }
                }
        ).subscribeOn(Schedulers.io()).subscribe(SyncObserver(params))
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        disposable.dispose()
        return true
    }

    inner class SyncObserver(private val params: JobParameters) : CompletableObserver {
        override fun onComplete() {
            jobFinished(params, false)
        }

        override fun onSubscribe(d: Disposable) {
            disposable = d
        }

        override fun onError(e: Throwable) {
            logger.error(e)
            jobFinished(params, true)
        }
    }
}
