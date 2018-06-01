package org.watsi.uhp.services

import android.app.job.JobParameters
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.watsi.device.managers.Logger
import org.watsi.domain.usecases.SyncEncounterFormUseCase
import org.watsi.domain.usecases.SyncMemberPhotoUseCase
import javax.inject.Inject

class SyncPhotosService : DaggerJobService() {

    @Inject lateinit var syncMemberPhotoUseCase: SyncMemberPhotoUseCase
    @Inject lateinit var syncEncounterFormUseCase: SyncEncounterFormUseCase
    @Inject lateinit var logger: Logger
    private lateinit var disposable: Disposable
    private var errored = false

    override fun onStartJob(params: JobParameters): Boolean {
        Completable.concatArray(
                syncMemberPhotoUseCase.execute().onErrorComplete { setError(it) },
                syncEncounterFormUseCase.execute().onErrorComplete { setError(it) },
                Completable.fromAction { if (errored) { throw Exception() } }
        ).subscribeOn(Schedulers.io()).subscribe(SyncObserver(params))
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        disposable.dispose()
        return true
    }

    private fun setError(e: Throwable): Boolean {
        errored = true
        logger.error(e)
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
