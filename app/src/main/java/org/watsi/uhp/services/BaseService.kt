package org.watsi.uhp.services

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import dagger.android.AndroidInjection
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.watsi.device.managers.Logger
import org.watsi.uhp.BuildConfig
import javax.inject.Inject

/**
 * Base class that defines default job behaviors and provides an overridable method to specify
 * what tasks to run.
 */
abstract class BaseService : JobService() {

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

    /**
     * Allows executeTasks() to keep running subsequent tasks even if previous ones error, but
     * still log and track the errors so that onError in SyncObserver is called and the job is
     * automatically re-run.
     */
    fun setErrored(e: Throwable): Boolean {
        errored = true
        logger.error(e)
        return true
    }

    fun getErrored(): Boolean {
        return errored
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
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

    companion object {
        private const val SYNC_INTERVAL = 15 * 60 * 1000 // 15 minutes (JobScheduler minimum)

        fun <T : BaseService> schedule(jobId: Int, context: Context, jobClass: Class<T>) {
            val requireNetwork = BuildConfig.BUILD_TYPE == "release"

            this::class.java
            val jobInfo = JobInfo.Builder(jobId, ComponentName(context, jobClass))
                    .setRequiredNetworkType(if (requireNetwork) JobInfo.NETWORK_TYPE_ANY else JobInfo.NETWORK_TYPE_NONE)
                    .setPeriodic(SYNC_INTERVAL.toLong())
                    .setPersisted(true)
                    .build()

            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(jobInfo)
        }
    }
}
