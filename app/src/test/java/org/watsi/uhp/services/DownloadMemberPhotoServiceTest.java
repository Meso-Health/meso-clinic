package org.watsi.uhp.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExceptionManager.class, FileManager.class, MemberDao.class })
public class DownloadMemberPhotoServiceTest {

    private DownloadMemberPhotosService service;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(ExceptionManager.class);
        mockStatic(FileManager.class);
        mockStatic(MemberDao.class);
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

    @Test
    public void fetchMemberPhotos_memberHasRemotePhoto() throws Exception {
        Member member = mock(Member.class);
        List<Member> memberList = new ArrayList<>();
        memberList.add(member);

        when(MemberDao.membersWithPhotosToFetch()).thenReturn(memberList);
        when(FileManager.isLocal(anyString())).thenReturn(false);

        service.fetchMemberPhotos();

        verify(member, times(1)).fetchAndSetPhotoFromUrl();
        verify(member, times(1)).updateFromFetch();
    }

    @Test
    public void fetchMemberPhotos_memberHasLocalPhoto() throws Exception {
        Member member = mock(Member.class);
        List<Member> memberList = new ArrayList<>();
        memberList.add(member);

        when(MemberDao.membersWithPhotosToFetch()).thenReturn(memberList);
        when(FileManager.isLocal(anyString())).thenReturn(true);

        service.fetchMemberPhotos();

        verify(member, never()).fetchAndSetPhotoFromUrl();
    }
}
