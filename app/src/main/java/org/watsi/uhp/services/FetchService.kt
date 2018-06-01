package org.watsi.uhp.services

import android.app.job.JobParameters
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import org.watsi.device.managers.Logger
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DiagnosisRepository
import org.watsi.domain.repositories.MemberRepository
import javax.inject.Inject

/**
 * Service class that polls the UHP API and updates the device with updated member and billables data
 */
class FetchService : DaggerJobService() {

    @Inject lateinit var memberRepository: MemberRepository
    @Inject lateinit var billableRepository: BillableRepository
    @Inject lateinit var diagnosisRepository: DiagnosisRepository
    @Inject lateinit var logger: Logger
    private lateinit var disposable: Disposable
    private var errors = mutableListOf<Throwable>()

    override fun onStartJob(params: JobParameters): Boolean {
        Completable.concatArray(
                memberRepository.fetch().onErrorComplete { errors.add(it) },
                billableRepository.fetch().onErrorComplete { errors.add(it) },
                diagnosisRepository.fetch().onErrorComplete { errors.add(it) },
                Completable.fromAction {
                    if (errors.size > 0) { throw Exception(errors.map { it.message }.joinToString()) }
                }
        ).subscribe(SyncObserver(params))
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
