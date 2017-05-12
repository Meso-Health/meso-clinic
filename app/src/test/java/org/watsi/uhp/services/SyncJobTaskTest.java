package org.watsi.uhp.services;

import android.app.job.JobParameters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SyncJobTaskTest {

    @Mock
    JobParameters mockJobParameters;
    @Mock
    AbstractSyncJobService mockService;

    SyncJobTask syncJobTask;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        syncJobTask = new SyncJobTask(mockService, mockJobParameters);
    }

    @Test
    public void doInBackground() throws Exception {
        syncJobTask.doInBackground();

        verify(mockService, times(1)).performSync();
    }

    @Test
    public void onPostExecute() throws Exception {
        syncJobTask.onPostExecute(true);

        verify(mockService, times(1)).jobFinished(mockJobParameters, false);
    }
}
