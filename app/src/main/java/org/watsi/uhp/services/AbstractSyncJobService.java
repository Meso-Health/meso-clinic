package org.watsi.uhp.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.os.AsyncTask;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.managers.ExceptionManager;

import dagger.android.AndroidInjection;

public abstract class AbstractSyncJobService extends JobService {

    private static int SYNC_INTERVAL = 15 * 60 * 1000; // 15 minutes (JobScheduler minimum)

    private SyncJobTask mSyncJobTask = null;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        mSyncJobTask = new SyncJobTask(this, params);
        mSyncJobTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        ExceptionManager.reportMessage(this.getClass().getSimpleName() + ": called onStopJob");
        if (getSyncJobTask() != null) getSyncJobTask().cancel(true);
        return false;
    }

    public static JobInfo buildJobInfo(int jobId, ComponentName componentName) {
        return buildJobInfo(jobId, componentName, !BuildConfig.USING_LOCAL_SERVER);
    }

    public static JobInfo buildJobInfo(int jobId, ComponentName componentName, boolean requireNetwork) {
        return new JobInfo.Builder(jobId, componentName)
                .setRequiredNetworkType(requireNetwork ? JobInfo.NETWORK_TYPE_ANY : JobInfo.NETWORK_TYPE_NONE)
                .setPeriodic(SYNC_INTERVAL)
                .setPersisted(true)
                .build();
    }

    /**
     * Implement with the logic that performs the data syncing. Method is called within
     * mSycJobTask and will be performed in a background thread.
     *
     * @return True indicates the sync completed successfully and false indicates
     * the sync process failed so the job should be rescheduled
     */
    public abstract boolean performSync();

    public SyncJobTask getSyncJobTask() {
        return mSyncJobTask;
    }
}
