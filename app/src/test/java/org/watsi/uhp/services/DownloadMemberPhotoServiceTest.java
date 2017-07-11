package org.watsi.uhp.services;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DownloadMemberPhotosService.class, ExceptionManager.class, FileManager.class,
        Log.class, MemberDao.class, OkHttpClient.Builder.class })
public class DownloadMemberPhotoServiceTest {

    private DownloadMemberPhotosService service;

    @Mock
    OkHttpClient mockHttpClient;
    @Mock
    OkHttpClient.Builder mockHttpClientBuilder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(ExceptionManager.class);
        mockStatic(FileManager.class);
        mockStatic(MemberDao.class);
        mockStatic(Log.class);
        service = new DownloadMemberPhotosService();
    }

    @Test
    public void performSync_fetchDoesNotThrowException() throws Exception {
        DownloadMemberPhotosService spiedService = spy(service);

        doNothing().when(spiedService).fetchMemberPhotos();

        boolean result = spiedService.performSync();

        assertTrue(result);
        verify(spiedService, times(1)).fetchMemberPhotos();
    }

    @Test
    public void performSync_fetchThrowsException() throws Exception {
        DownloadMemberPhotosService spiedService = spy(service);
        SQLException mockException = mock(SQLException.class);

        doThrow(mockException).when(spiedService).fetchMemberPhotos();

        boolean result = spiedService.performSync();

        assertFalse(result);
        verifyStatic(times(1));
        ExceptionManager.reportException(mockException);
    }

    private void mockHttpClient() throws Exception {
        whenNew(OkHttpClient.Builder.class).withNoArguments().thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.connectTimeout(ApiService.HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS))
                .thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.readTimeout(ApiService.HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS))
                .thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.writeTimeout(ApiService.HTTP_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS))
                .thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);

    }

    @Test
    public void fetchMemberPhotos_memberHasRemotePhoto() throws Exception {
        Member member = mock(Member.class);
        List<Member> memberList = new ArrayList<>();
        memberList.add(member);

        mockHttpClient();
        doNothing().when(member).fetchAndSetPhotoFromUrl(mockHttpClient);
        when(MemberDao.membersWithPhotosToFetch()).thenReturn(memberList);
        when(FileManager.isLocal(anyString())).thenReturn(false);

        service.fetchMemberPhotos();

        verify(member, times(1)).fetchAndSetPhotoFromUrl(mockHttpClient);
        verify(member, times(1)).updateFromFetch();
    }

    @Test
    public void fetchMemberPhotos_memberHasLocalPhoto() throws Exception {
        Member member = mock(Member.class);
        List<Member> memberList = new ArrayList<>();
        memberList.add(member);

        mockHttpClient();
        doNothing().when(member).fetchAndSetPhotoFromUrl(mockHttpClient);
        when(MemberDao.membersWithPhotosToFetch()).thenReturn(memberList);
        when(FileManager.isLocal(anyString())).thenReturn(true);

        service.fetchMemberPhotos();

        verify(member, never()).fetchAndSetPhotoFromUrl(mockHttpClient);
    }
}
