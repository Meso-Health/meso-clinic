package org.watsi.uhp.services;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.content.ComponentName;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.managers.ExceptionManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AbstractSyncJobService.class, DatabaseHelper.class, ExceptionManager.class })
public class AbstractSyncJobServiceTest {

    @Mock
    JobParameters mockJobParameters;
    @Mock
    ComponentName mockComponentName;
    @Mock
    JobInfo.Builder mockBuilder;
    @Mock
    JobInfo mockJobInfo;
    @Mock
    SyncJobTask mockSyncJobTask;

    // using a concrete implementation to test the abstract class
    FetchService fetchService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(DatabaseHelper.class);
        fetchService = new FetchService();
    }

    @Test
    public void onStartJob_initializesDatabaseHelper() throws Exception {
        whenNew(SyncJobTask.class).withArguments(fetchService, mockJobParameters)
                .thenReturn(mockSyncJobTask);

        fetchService.onStartJob(mockJobParameters);

        verifyStatic();
        DatabaseHelper.init(fetchService);
    }

    @Test
    public void onStartJob_executesSyncJobTask() throws Exception {
        whenNew(SyncJobTask.class).withArguments(fetchService, mockJobParameters)
                .thenReturn(mockSyncJobTask);

        boolean result = fetchService.onStartJob(mockJobParameters);

        assertFalse(result);
        verify(mockSyncJobTask, times(1)).execute();
    }

    @Test
    public void onStopJob() throws Exception {
        FetchService fetchServiceSpy = spy(fetchService);
        when(fetchServiceSpy.getSyncJobTask()).thenReturn(mockSyncJobTask);
        mockStatic(ExceptionManager.class);

        boolean result = fetchServiceSpy.onStopJob(mockJobParameters);

        assertFalse(result);
        verify(mockSyncJobTask, times(1)).cancel(true);
        verifyStatic(times(1));
        ExceptionManager.reportMessage(anyString());
    }

    @Test
    public void buildJobInfo() throws Exception {
        int jobId = 0;
        whenNew(JobInfo.Builder.class).withArguments(jobId, mockComponentName).thenReturn(mockBuilder);
        when(mockBuilder.setRequiredNetworkType(anyInt())).thenReturn(mockBuilder);
        when(mockBuilder.setPeriodic(anyInt())).thenReturn(mockBuilder);
        when(mockBuilder.setPersisted(anyBoolean())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockJobInfo);

        JobInfo result = FetchService.buildJobInfo(jobId, mockComponentName);

        assertEquals(result, mockJobInfo);
        verify(mockBuilder, times(1)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        verify(mockBuilder, times(1)).setPeriodic(15 * 60 * 1000);
        verify(mockBuilder, times(1)).setPersisted(true);
    }
}
