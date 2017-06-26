package org.watsi.uhp.services;

import android.net.Uri;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterForm;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Response;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
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
@PrepareForTest({ Encounter.class, EncounterDao.class, EncounterItemDao.class,
        ExceptionManager.class, File.class, FileManager.class, IdentificationEvent.class,
        MediaType.class, Member.class, okhttp3.Response.class, RequestBody.class, Response.class,
        SyncableModel.class, SyncService.class, Uri.class, Log.class })
public class SyncServiceTest {
    @Mock
    List<IdentificationEvent> mockIdentificationEventsList;
    @Mock
    List<Encounter> mockEncountersList;
    @Mock
    List<EncounterForm> mockEncounterFormsList;
    @Mock
    List<Member> mockMembersList;
    @Mock
    okhttp3.Response mockRawResponse;
    @Mock
    Response<IdentificationEvent> mockIdentificationEventSyncResponse;
    @Mock
    Response<Encounter> mockEncounterSyncResponse;
    @Mock
    Response<Member> mockMemberSyncResponse;

    private SyncService syncService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(ExceptionManager.class);
        mockStatic(Encounter.class);
        mockStatic(EncounterDao.class);
        mockStatic(EncounterItemDao.class);
        mockStatic(FileManager.class);
        mockStatic(IdentificationEvent.class);
        mockStatic(MediaType.class);
        mockStatic(Member.class);
        mockStatic(SyncableModel.class);
        mockStatic(Uri.class);
        mockStatic(Log.class);
        syncService = new SyncService();
    }

    @Test
    public void performSync_fetchDoesNotThrowException() throws Exception {
        SyncService spiedSyncService = spy(syncService);

        PowerMockito.when(SyncableModel.class, "unsynced", IdentificationEvent.class)
                .thenReturn(mockIdentificationEventsList);
        PowerMockito.when(SyncableModel.class, "unsynced", Encounter.class)
                .thenReturn(mockEncountersList);
        PowerMockito.when(SyncableModel.class, "unsynced", EncounterForm.class)
                .thenReturn(mockEncounterFormsList);
        PowerMockito.when(SyncableModel.class, "unsynced", Member.class)
                .thenReturn(mockMembersList);
        doNothing().when(spiedSyncService).syncIdentificationEvents(
                anyListOf(IdentificationEvent.class));
        doNothing().when(spiedSyncService).syncEncounters(anyListOf(Encounter.class));
        doNothing().when(spiedSyncService).syncEncounterForms(anyListOf(EncounterForm.class));
        doNothing().when(spiedSyncService).syncMembers(anyListOf(Member.class));

        boolean result = spiedSyncService.performSync();

        assertTrue(result);
        verify(spiedSyncService, times(1)).syncIdentificationEvents(mockIdentificationEventsList);
        verify(spiedSyncService, times(1)).syncEncounters(mockEncountersList);
        verify(spiedSyncService, times(1)).syncEncounterForms(mockEncounterFormsList);
        verify(spiedSyncService, times(1)).syncMembers(mockMembersList);
    }

    @Test
    public void performSync_fetchThrowsException() throws Exception {
        SyncService spiedSyncService = spy(SyncService.class);
        SQLException mockException = mock(SQLException.class);

        when(IdentificationEvent.unsynced(IdentificationEvent.class))
                .thenReturn(mockIdentificationEventsList);
        doThrow(mockException).when(spiedSyncService)
                .syncIdentificationEvents(mockIdentificationEventsList);

        boolean result = spiedSyncService.performSync();

        assertFalse(result);
        verifyStatic(times(1));
        ExceptionManager.reportException(mockException);
    }

    private Member mockMember() {
        Member mockMember = mock(Member.class);
        when(mockMember.getId()).thenReturn(UUID.randomUUID());
        return mockMember;
    }

    private IdentificationEvent mockIdentificationEvent() throws Exception {
        IdentificationEvent mockIdentificationEvent = mock(IdentificationEvent.class);
        when(mockIdentificationEvent.getId()).thenReturn(UUID.randomUUID());
        Member mockMember = mockMember();
        when(mockIdentificationEvent.getMember()).thenReturn(mockMember);
        return mockIdentificationEvent;
    }

    @Test
    public void syncIdentificationEvents_succeeds() throws Exception {
        IdentificationEvent mockIdentificationEvent = mockIdentificationEvent();
        List<IdentificationEvent> identificationEventList = new ArrayList<>();
        identificationEventList.add(mockIdentificationEvent);

        when(mockIdentificationEvent.sync(syncService))
                .thenReturn(mockIdentificationEventSyncResponse);
        when(mockIdentificationEventSyncResponse.isSuccessful()).thenReturn(true);

        syncService.syncIdentificationEvents(identificationEventList);

        verify(mockIdentificationEvent, times(1)).setMemberId(any(UUID.class));
        verify(mockIdentificationEvent, times(1))
                .updateFromSync(mockIdentificationEventSyncResponse);
    }

    @Test
    public void syncIdentificationEvents_fails() throws Exception {
        IdentificationEvent mockIdentificationEvent = mockIdentificationEvent();
        List<IdentificationEvent> identificationEventList = new ArrayList<>();
        identificationEventList.add(mockIdentificationEvent);

        when(mockIdentificationEvent.sync(syncService))
                .thenReturn(mockIdentificationEventSyncResponse);
        when(mockIdentificationEventSyncResponse.isSuccessful()).thenReturn(false);
        when(mockIdentificationEventSyncResponse.raw()).thenReturn(mockRawResponse);

        syncService.syncIdentificationEvents(identificationEventList);

        verify(mockIdentificationEvent, never())
                .updateFromSync(mockIdentificationEventSyncResponse);
        verifyStatic();
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class),
                anyMapOf(String.class, String.class));
    }

    private Encounter mockEncounter() throws Exception {
        Encounter mockEncounter = mock(Encounter.class);
        when(mockEncounter.getId()).thenReturn(UUID.randomUUID());
        Member mockMember = mockMember();
        when(mockEncounter.getMember()).thenReturn(mockMember);
        IdentificationEvent mockIdentificationEvent = mockIdentificationEvent();
        when(mockEncounter.getIdentificationEvent()).thenReturn(mockIdentificationEvent);
        return mockEncounter;
    }

    @Test
    public void syncEncounters_isSuccessful() throws Exception {
        Encounter mockEncounter = mockEncounter();
        List<Encounter> encountersList = new ArrayList<>();
        encountersList.add(mockEncounter);

        when(mockEncounter.sync(syncService)).thenReturn(mockEncounterSyncResponse);
        when(mockEncounterSyncResponse.isSuccessful()).thenReturn(true);

        syncService.syncEncounters(encountersList);

        verify(mockEncounter, times(1)).updateFromSync(mockEncounterSyncResponse);
    }

    @Test
    public void syncEncounters_fails() throws Exception {
        Encounter mockEncounter = mockEncounter();
        List<Encounter> encountersList = new ArrayList<>();
        encountersList.add(mockEncounter);

        when(mockEncounter.sync(syncService)).thenReturn(mockEncounterSyncResponse);
        when(mockEncounterSyncResponse.isSuccessful()).thenReturn(false);
        when(mockEncounterSyncResponse.raw()).thenReturn(mockRawResponse);

        syncService.syncEncounters(encountersList);

        verify(mockEncounter, never()).updateFromSync(any(Response.class));
        verifyStatic();
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class),
                anyMapOf(String.class, String.class));
    }

    private EncounterForm mockEncounterForm(boolean encounterSynced) throws Exception {
        EncounterForm mockEncounterForm = mock(EncounterForm.class);
        when(mockEncounterForm.getId()).thenReturn(UUID.randomUUID());
        Encounter mockEncounter = mockEncounter();
        when(mockEncounter.isSynced()).thenReturn(encounterSynced);
        when(EncounterDao.find(mockEncounter.getId())).thenReturn(mockEncounter);
        when(mockEncounterForm.getEncounter()).thenReturn(mockEncounter);
        return mockEncounterForm;
    }

    @Test
    public void syncEncounterForms_associatedEncounterIsNotSynced() throws Exception {
        EncounterForm mockEncounterForm = mockEncounterForm(false);
        List<EncounterForm> encounterFormsList = new ArrayList<>();
        encounterFormsList.add(mockEncounterForm);
        Encounter mockEncounter = mockEncounterForm.getEncounter();

        when(EncounterDao.find(mockEncounter.getId())).thenReturn(mockEncounter);
        when(mockEncounterForm(false).getEncounter().isSynced()).thenReturn(false);

        syncService.syncEncounterForms(encounterFormsList);

        verify(mockEncounterForm, never()).sync(syncService);
    }

    @Test
    public void syncEncounterForms_associatedEncounterSyncedAndNullImage() throws Exception {
        EncounterForm mockEncounterForm = mockEncounterForm(true);
        List<EncounterForm> encounterFormsList = new ArrayList<>();
        encounterFormsList.add(mockEncounterForm);

        syncService.syncEncounterForms(encounterFormsList);

        verify(mockEncounterForm, never()).sync(syncService);
        verify(mockEncounterForm, times(1)).delete();
    }

    @Test
    public void syncEncounterForms_associatedEncounterSyncedWithImage_succeeds() throws Exception {
        EncounterForm mockEncounterForm = mockEncounterForm(true);
        List<EncounterForm> encounterFormsList = new ArrayList<>();
        encounterFormsList.add(mockEncounterForm);
        byte[] image = new byte[]{(byte)0xe0};

        when(mockEncounterForm.getImage(syncService)).thenReturn(image);
        when(mockEncounterForm.sync(syncService)).thenReturn(mockEncounterSyncResponse);
        when(mockEncounterSyncResponse.isSuccessful()).thenReturn(true);

        syncService.syncEncounterForms(encounterFormsList);

        verify(mockEncounterForm, times(1)).updateFromSync(mockEncounterSyncResponse);
    }

    @Test
    public void syncEncounterForms_associatedEncounterSyncedWithImage_fails() throws Exception {
        EncounterForm mockEncounterForm = mockEncounterForm(true);
        List<EncounterForm> encounterFormsList = new ArrayList<>();
        encounterFormsList.add(mockEncounterForm);
        byte[] image = new byte[]{(byte)0xe0};

        when(mockEncounterForm.getImage(syncService)).thenReturn(image);
        when(mockEncounterForm.sync(syncService)).thenReturn(mockEncounterSyncResponse);
        when(mockEncounterSyncResponse.isSuccessful()).thenReturn(false);
        when(mockEncounterSyncResponse.raw()).thenReturn(mockRawResponse);

        syncService.syncEncounterForms(encounterFormsList);

        verify(mockEncounterForm, never()).updateFromSync(any(Response.class));
        verifyStatic();
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class),
                anyMapOf(String.class, String.class));
    }

    @Test
    public void syncMembers_succeeds() throws Exception {
        Member mockMember = mockMember();
        List<Member> membersList = new ArrayList<>();
        membersList.add(mockMember);

        when(mockMember.sync(syncService)).thenReturn(mockMemberSyncResponse);
        when(mockMemberSyncResponse.isSuccessful()).thenReturn(true);

        syncService.syncMembers(membersList);

        verify(mockMember, times(1)).updateFromSync(mockMemberSyncResponse);
    }

    @Test
    public void syncMembers_fails() throws Exception {
        Member mockMember = mockMember();
        List<Member> membersList = new ArrayList<>();
        membersList.add(mockMember);

        when(mockMember.sync(syncService)).thenReturn(mockMemberSyncResponse);
        when(mockMemberSyncResponse.isSuccessful()).thenReturn(false);
        when(mockMemberSyncResponse.raw()).thenReturn(mockRawResponse);

        syncService.syncMembers(membersList);

        verify(mockMember, never()).updateFromSync(mockMemberSyncResponse);
        verifyStatic();
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class),
                anyMapOf(String.class, String.class));
    }
}
