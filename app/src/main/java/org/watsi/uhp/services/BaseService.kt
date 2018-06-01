package org.watsi.uhp.services

import android.app.job.JobParameters
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.watsi.device.managers.Logger
import javax.inject.Inject

abstract class BaseService : DaggerJobService() {

    @Inject lateinit var logger: Logger
    private lateinit var disposable: Disposable
    private var errored = false

    override fun onStartJob(params: JobParameters): Boolean {
        executeTasks().subscribeOn(Schedulers.io()).subscribe(SyncObserver(params))
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        disposable.dispose()
        return true
    }

    abstract fun executeTasks(): Completable

    fun setErrored(e: Throwable): Boolean {
        errored = true
        logger.error(e)
        return true
    }

    fun getErrored(): Boolean {
        return errored
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
