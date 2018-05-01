package org.watsi.uhp.services

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import dagger.android.AndroidInjection
import org.watsi.uhp.BuildConfig

abstract class DaggerJobService : JobService() {

    companion object {
        private const val SYNC_INTERVAL = 15 * 60 * 1000 // 15 minutes (JobScheduler minimum)

        fun <T : DaggerJobService> schedule(jobId: Int, context: Context, jobClass: Class<T>) {
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

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }
}
