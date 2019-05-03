package org.watsi.uhp.services

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.watsi.device.managers.Logger
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R
import org.watsi.uhp.helpers.NetworkErrorHelper
import javax.inject.Inject

/**
 * Base class that defines default job behaviors and provides an overridable method to specify
 * what tasks to run.
 */
abstract class BaseService : JobService() {

    @Inject lateinit var logger: Logger
    private lateinit var disposable: Disposable
    private val errorMessages = mutableListOf<String>()

    override fun onStartJob(params: JobParameters): Boolean {
        broadcastJobStarted()
        executeTasks().subscribeOn(Schedulers.io()).subscribe(SyncObserver(params))
        return true
    }

    // Called when system forcefully stops job before it finishes
    override fun onStopJob(params: JobParameters): Boolean {
        broadcastJobEnded()
        disposable.dispose()
        return true
    }

    abstract fun executeTasks(): Completable

    fun broadcastJobStarted() {
        val intent = Intent()
        intent.action = ACTION_SERVICE_UPDATE
        intent.putExtra(PARAM_SERVICE_CLASS, javaClass.toString())
        intent.putExtra(PARAM_IS_RUNNING, true)

        sendBroadcast(intent)
    }

    fun broadcastJobEnded() {
        val intent = Intent()
        intent.action = ACTION_SERVICE_UPDATE
        intent.putExtra(PARAM_SERVICE_CLASS, javaClass.toString())
        intent.putExtra(PARAM_IS_RUNNING, false)
        if (getErrorMessages().isNotEmpty()) {
            intent.putStringArrayListExtra(PARAM_ERRORS, ArrayList(getErrorMessages()))
        }

        sendBroadcast(intent)
    }


    /**
     * Allows executeTasks() to keep running subsequent tasks even if previous ones error, but
     * still log and track the errors so that onError in SyncObserver is called and the job is
     * automatically re-run.
     */
    fun setError(e: Throwable, label: String): Boolean {
        // Unwrap exception (some exceptions are wrapped / chained / rethrown as RuntimeExceptions)
        val error = e.cause ?: e
        when {
            NetworkErrorHelper.isHttpUnauthorized(error) -> {
                errorMessages.add("$label: ${error.message}. ${getString(R.string.credentials_expired_error_message)}.")
                logger.warning(error)
            }
            // Service can still be run if phone is offline for debug variant
            NetworkErrorHelper.isPhoneOfflineError(error) -> {
                errorMessages.add("$label: ${error.message}. ${getString(R.string.phone_offline_error_message)}.")
                logger.info(error)
            }
            NetworkErrorHelper.isServerOfflineError(error) -> {
                errorMessages.add("$label: ${error.message}. ${getString(R.string.server_offline_error_message)}.")
                logger.warning(error)
            }
            NetworkErrorHelper.isPoorConnectivityError(error) -> {
                errorMessages.add("$label: ${error.message}. ${getString(R.string.poor_connectivity_error_message)}.")
                logger.info(error)
            }
            else -> {
                errorMessages.add("$label: ${error.message}")
                logger.error(error)
            }
        }
        return true
    }

    fun getErrorMessages(): List<String> {
        return errorMessages
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    inner class SyncObserver(private val params: JobParameters) : CompletableObserver {
        override fun onComplete() {
            broadcastJobEnded()
            if (!getErrorMessages().isEmpty()) {
                jobFinished(params, true)
            } else {
                jobFinished(params, false)
            }
        }

        override fun onSubscribe(d: Disposable) {
            disposable = d
        }

        override fun onError(e: Throwable) {
            logger.error(e)
            broadcastJobEnded()
            jobFinished(params, true)
        }
    }

    companion object {
        private const val SYNC_INTERVAL = 15 * 60 * 1000 // 15 minutes (JobScheduler minimum)
        const val ACTION_SERVICE_UPDATE = "${BuildConfig.APPLICATION_ID}.action.SERVICE_UPDATE"
        const val PARAM_SERVICE_CLASS = "service_class"
        const val PARAM_IS_RUNNING = "is_running"
        const val PARAM_ERRORS = "errors"

        fun <T : BaseService> schedule(jobId: Int, context: Context, jobClass: Class<T>) {
            val requireNetwork = BuildConfig.BUILD_TYPE != "debug"

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
