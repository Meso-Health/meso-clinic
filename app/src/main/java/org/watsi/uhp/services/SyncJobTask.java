package org.watsi.uhp.services;

import android.app.job.JobParameters;
import android.os.AsyncTask;

import org.watsi.uhp.managers.ExceptionManager;

class SyncJobTask extends AsyncTask<Void, Void, Boolean> {

    private final JobParameters mJobParameters;
    private final AbstractSyncJobService mService;

    SyncJobTask(AbstractSyncJobService service, JobParameters jobParameters) {
        this.mService = service;
        this.mJobParameters = jobParameters;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            return mService.performSync();
        } catch (RuntimeException e) {
            ExceptionManager.reportException(e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean successful) {
        mService.jobFinished(mJobParameters, !successful);
    }
}
