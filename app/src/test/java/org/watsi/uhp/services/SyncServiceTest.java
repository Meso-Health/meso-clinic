package org.watsi.uhp.services;

import android.net.Uri;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.api.UhpApi;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.EncounterFormDao;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterForm;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApiService.class, EncounterDao.class, EncounterFormDao.class,
        EncounterItemDao.class, ExceptionManager.class, File.class, FileManager.class,
        IdentificationEventDao.class, MediaType.class, MemberDao.class, okhttp3.Response.class,
        RequestBody.class, Response.class, SyncService.class, Uri.class })
public class SyncServiceTest {
    private final String REMOTE_PHOTO_URL = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
    private final String LOCAL_PHOTO_URL = "https://d2bxcwowl6jlve.cloudfront.net/media/foo-3bf77f20d8119074";

    @Mock
    UhpApi mockApi;
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
    Call<IdentificationEvent> mockIdentificationEventCall;
    @Mock
    Response<IdentificationEvent> mockIdentificationEventSyncResponse;
    @Mock
    Call<Encounter> mockEncounterCall;
    @Mock
    Response<Encounter> mockEncounterSyncResponse;
    @Mock
    Call<Member> mockMemberCall;
    @Mock
    Response<Member> mockMemberSyncResponse;
    @Mock
    HashMap<String, RequestBody> mockRequestBodyMap;

    private SyncService syncService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(ApiService.class);
        mockStatic(ExceptionManager.class);
        mockStatic(EncounterDao.class);
        mockStatic(EncounterFormDao.class);
        mockStatic(EncounterItemDao.class);
        mockStatic(FileManager.class);
        mockStatic(IdentificationEventDao.class);
        mockStatic(MediaType.class);
        mockStatic(MemberDao.class);
        mockStatic(Uri.class);
        syncService = new SyncService();
    }

    @Test
    public void performSync_fetchDoesNotThrowException() throws Exception {
        SyncService spiedSyncService = spy(syncService);

        when(IdentificationEventDao.unsynced()).thenReturn(mockIdentificationEventsList);
        when(EncounterDao.unsynced()).thenReturn(mockEncountersList);
        when(EncounterFormDao.unsynced()).thenReturn(mockEncounterFormsList);
        when(MemberDao.unsynced()).thenReturn(mockMembersList);
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

        when(IdentificationEventDao.unsynced()).thenReturn(mockIdentificationEventsList);
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
        when(mockMember.getTokenAuthHeaderString()).thenReturn("Token foo");
        return mockMember;
    }

    private IdentificationEvent mockIdentificationEvent(boolean isNew) {
        IdentificationEvent mockIdentificationEvent = mock(IdentificationEvent.class);
        when(mockIdentificationEvent.getTokenAuthHeaderString()).thenReturn("Token foo");
        when(mockIdentificationEvent.getId()).thenReturn(UUID.randomUUID());
        when(mockIdentificationEvent.isNew()).thenReturn(isNew);
        Member mockMember = mockMember();
        when(mockIdentificationEvent.getMember()).thenReturn(mockMember);
        return mockIdentificationEvent;
    }

    @Test
    public void syncIdentificationEvents_newEvent_succeeds() throws Exception {
        SyncService spiedService = spy(syncService);
        IdentificationEvent mockIdentificationEvent = mockIdentificationEvent(true);
        List<IdentificationEvent> identificationEventList = new ArrayList<>();
        identificationEventList.add(mockIdentificationEvent);

        doReturn(mockIdentificationEventSyncResponse).when(spiedService)
                .postIdentificationEvent(mockIdentificationEvent);
        when(mockIdentificationEventSyncResponse.isSuccessful()).thenReturn(true);

        spiedService.syncIdentificationEvents(identificationEventList);

        verify(mockIdentificationEvent, times(1)).setSynced();
        verifyStatic();
        IdentificationEventDao.update(mockIdentificationEvent);
    }

    @Test
    public void syncIdentificationEvents_newEvent_fails() throws Exception {
        SyncService spiedService = spy(syncService);
        IdentificationEvent mockIdentificationEvent = mockIdentificationEvent(true);
        List<IdentificationEvent> identificationEventList = new ArrayList<>();
        identificationEventList.add(mockIdentificationEvent);

        doReturn(mockIdentificationEventSyncResponse).when(spiedService)
                .postIdentificationEvent(mockIdentificationEvent);
        when(mockIdentificationEventSyncResponse.isSuccessful()).thenReturn(false);
        when(mockIdentificationEventSyncResponse.raw()).thenReturn(mockRawResponse);

        spiedService.syncIdentificationEvents(identificationEventList);

        verify(mockIdentificationEvent, never()).setSynced();
        verifyStatic(never());
        IdentificationEventDao.update(mockIdentificationEvent);
        verifyStatic();
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class),
                anyMapOf(String.class, String.class));
    }

    @Test
    public void syncIdentificationEvents_updatedEvent_succeeds() throws Exception {
        SyncService spiedService = spy(syncService);
        IdentificationEvent mockIdentificationEvent = mockIdentificationEvent(false);
        List<IdentificationEvent> identificationEventList = new ArrayList<>();
        identificationEventList.add(mockIdentificationEvent);

        doReturn(mockIdentificationEventSyncResponse).when(spiedService)
                .patchIdentificationEvent(mockIdentificationEvent);
        when(mockIdentificationEventSyncResponse.isSuccessful()).thenReturn(true);

        spiedService.syncIdentificationEvents(identificationEventList);

        verify(mockIdentificationEvent, times(1)).setSynced();
        verifyStatic();
        IdentificationEventDao.update(mockIdentificationEvent);
    }

    @Test
    public void syncIdentificationEvents_updatedEvent_fails() throws Exception {
        SyncService spiedService = spy(syncService);
        IdentificationEvent mockIdentificationEvent = mockIdentificationEvent(false);
        List<IdentificationEvent> identificationEventList = new ArrayList<>();
        identificationEventList.add(mockIdentificationEvent);

        doReturn(mockIdentificationEventSyncResponse).when(spiedService)
                .patchIdentificationEvent(mockIdentificationEvent);
        when(mockIdentificationEventSyncResponse.isSuccessful()).thenReturn(false);
        when(mockIdentificationEventSyncResponse.raw()).thenReturn(mockRawResponse);

        spiedService.syncIdentificationEvents(identificationEventList);

        verify(mockIdentificationEvent, never()).setSynced();
        verifyStatic(never());
        IdentificationEventDao.update(mockIdentificationEvent);
        verifyStatic();
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class),
                anyMapOf(String.class, String.class));
    }

    @Test
    public void postIdentificationEvent_noThroughMember() throws Exception {
        IdentificationEvent mockIdentificationEvent = mockIdentificationEvent(true);

        when(mockIdentificationEvent.getThroughMember()).thenReturn(null);
        when(ApiService.requestBuilder(syncService)).thenReturn(mockApi);
        when(mockApi.postIdentificationEvent(
                mockIdentificationEvent.getTokenAuthHeaderString(), BuildConfig.PROVIDER_ID,
                mockIdentificationEvent)).thenReturn(mockIdentificationEventCall);
        when(mockIdentificationEventCall.execute()).thenReturn(mockIdentificationEventSyncResponse);

        Response response = syncService.postIdentificationEvent(mockIdentificationEvent);

        assertEquals(response, mockIdentificationEventSyncResponse);
        verify(mockIdentificationEventCall, times(1)).execute();
        verify(mockIdentificationEvent, never()).setThroughMemberId(any(UUID.class));
    }

    @Test
    public void postIdentificationEvent_hasThroughMember() throws Exception {
        IdentificationEvent mockIdentificationEvent = mockIdentificationEvent(true);
        Member mockThroughMember = mockMember();

        when(mockIdentificationEvent.getThroughMember()).thenReturn(mockThroughMember);
        when(ApiService.requestBuilder(syncService)).thenReturn(mockApi);
        when(mockApi.postIdentificationEvent(
                mockIdentificationEvent.getTokenAuthHeaderString(), BuildConfig.PROVIDER_ID,
                mockIdentificationEvent)).thenReturn(mockIdentificationEventCall);
        when(mockIdentificationEventCall.execute()).thenReturn(mockIdentificationEventSyncResponse);

        Response response = syncService.postIdentificationEvent(mockIdentificationEvent);

        assertEquals(response, mockIdentificationEventSyncResponse);
        verify(mockIdentificationEventCall, times(1)).execute();
        verify(mockIdentificationEvent, times(1)).setThroughMemberId(mockThroughMember.getId());
    }

    @Test
    public void patchIdentificationEvent() throws Exception {
        SyncService spiedService = spy(syncService);
        IdentificationEvent mockIdentificationEvent = mockIdentificationEvent(false);

        doReturn(mockRequestBodyMap).when(mockIdentificationEvent)
                .constructIdentificationEventPatchRequest();
        when(ApiService.requestBuilder(spiedService)).thenReturn(mockApi);
        when(mockApi.patchIdentificationEvent(
                anyString(), any(UUID.class), anyMapOf(String.class, RequestBody.class)))
                .thenReturn(mockIdentificationEventCall);
        when(mockIdentificationEventCall.execute()).thenReturn(mockIdentificationEventSyncResponse);

        Response<IdentificationEvent> response =
                spiedService.patchIdentificationEvent(mockIdentificationEvent);

        assertEquals(response, mockIdentificationEventSyncResponse);
        verify(mockApi, times(1)).patchIdentificationEvent(
                mockIdentificationEvent.getTokenAuthHeaderString(), mockIdentificationEvent.getId(),
                mockRequestBodyMap);
    }

    private Encounter mockEncounter() {
        Encounter mockEncounter = mock(Encounter.class);
        when(mockEncounter.getId()).thenReturn(UUID.randomUUID());
        when(mockEncounter.getTokenAuthHeaderString()).thenReturn("Token foo");
        Member mockMember = mockMember();
        when(mockEncounter.getMember()).thenReturn(mockMember);
        IdentificationEvent mockIdentificationEvent = mockIdentificationEvent(true);
        when(mockEncounter.getIdentificationEvent()).thenReturn(mockIdentificationEvent);
        return mockEncounter;
    }

    @Test
    public void syncEncounters_isSuccessful() throws Exception {
        Encounter mockEncounter = mockEncounter();
        List<Encounter> encountersList = new ArrayList<>();
        encountersList.add(mockEncounter);

        when(ApiService.requestBuilder(syncService)).thenReturn(mockApi);
        when(mockApi.syncEncounter(
                mockEncounter.getTokenAuthHeaderString(), BuildConfig.PROVIDER_ID, mockEncounter))
                .thenReturn(mockEncounterCall);
        when(mockEncounterCall.execute()).thenReturn(mockEncounterSyncResponse);
        when(mockEncounterSyncResponse.isSuccessful()).thenReturn(true);

        syncService.syncEncounters(encountersList);

        verify(mockEncounter, times(1)).setSynced();
        verifyStatic();
        EncounterDao.update(mockEncounter);
    }

    @Test
    public void syncEncounters_fails() throws Exception {
        Encounter mockEncounter = mockEncounter();
        List<Encounter> encountersList = new ArrayList<>();
        encountersList.add(mockEncounter);

        when(ApiService.requestBuilder(syncService)).thenReturn(mockApi);
        when(mockApi.syncEncounter(
                mockEncounter.getTokenAuthHeaderString(), BuildConfig.PROVIDER_ID, mockEncounter))
                .thenReturn(mockEncounterCall);
        when(mockEncounterCall.execute()).thenReturn(mockEncounterSyncResponse);
        when(mockEncounterSyncResponse.isSuccessful()).thenReturn(false);

        syncService.syncEncounters(encountersList);

        verify(mockEncounter, never()).setSynced();
        verifyStatic(never());
        EncounterDao.update(mockEncounter);
        verifyStatic();
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class),
                anyMapOf(String.class, String.class));
    }

    private EncounterForm mockEncounterForm(boolean encounterSynced) throws Exception {
        EncounterForm mockEncounterForm = mock(EncounterForm.class);
        when(mockEncounterForm.getId()).thenReturn(UUID.randomUUID());
        when(mockEncounterForm.getTokenAuthHeaderString()).thenReturn("Token foo");
        when(mockEncounterForm.getUrl()).thenReturn("foo");
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
        when(ApiService.requestBuilder(syncService)).thenReturn(mockApi);

        syncService.syncEncounterForms(encounterFormsList);

        verify(mockApi, never()).syncEncounterForm(
                anyString(), any(UUID.class), any(RequestBody.class));
    }

    @Test
    public void syncEncounterForms_associatedEncounterSyncedAndNullImage() throws Exception {
        EncounterForm mockEncounterForm = mockEncounterForm(true);
        List<EncounterForm> encounterFormsList = new ArrayList<>();
        encounterFormsList.add(mockEncounterForm);

        syncService.syncEncounterForms(encounterFormsList);

        verify(mockApi, never()).syncEncounterForm(
                anyString(), any(UUID.class), any(RequestBody.class));
        verify(mockEncounterForm, times(1)).setSynced();
        verifyStatic();
        EncounterFormDao.update(mockEncounterForm);
    }

    @Test
    public void syncEncounterForms_associatedEncounterSyncedWithImage_succeeds() throws Exception {
        EncounterForm mockEncounterForm = mockEncounterForm(true);
        List<EncounterForm> encounterFormsList = new ArrayList<>();
        encounterFormsList.add(mockEncounterForm);
        byte[] image = new byte[]{(byte)0xe0};
        MediaType mockImageMediaType = mock(MediaType.class);
        RequestBody mockRequestBody = mock(RequestBody.class);
        File mockFile = mock(File.class);
        mockStatic(RequestBody.class);

        when(mockEncounterForm.getImage(syncService)).thenReturn(image);
        when(ApiService.requestBuilder(syncService)).thenReturn(mockApi);
        when(MediaType.parse("image/jpg")).thenReturn(mockImageMediaType);
        when(RequestBody.create(mockImageMediaType, image)).thenReturn(mockRequestBody);
        when(mockApi.syncEncounterForm(
                mockEncounterForm.getTokenAuthHeaderString(),
                mockEncounterForm.getEncounter().getId(),
                mockRequestBody)).thenReturn(mockEncounterCall);
        when(mockEncounterCall.execute()).thenReturn(mockEncounterSyncResponse);
        when(mockEncounterSyncResponse.isSuccessful()).thenReturn(true);
        whenNew(File.class).withArguments(mockEncounterForm.getUrl()).thenReturn(mockFile);

        syncService.syncEncounterForms(encounterFormsList);

        verify(mockFile, times(1)).delete();
        verify(mockEncounterForm, times(1)).setSynced();
        verifyStatic();
        EncounterFormDao.update(mockEncounterForm);
    }

    @Test
    public void syncEncounterForms_associatedEncounterSyncedWithImage_fails() throws Exception {
        EncounterForm mockEncounterForm = mockEncounterForm(true);
        List<EncounterForm> encounterFormsList = new ArrayList<>();
        encounterFormsList.add(mockEncounterForm);
        byte[] image = new byte[]{(byte)0xe0};
        MediaType mockImageMediaType = mock(MediaType.class);
        RequestBody mockRequestBody = mock(RequestBody.class);
        File mockFile = mock(File.class);
        mockStatic(RequestBody.class);

        when(mockEncounterForm.getImage(syncService)).thenReturn(image);
        when(ApiService.requestBuilder(syncService)).thenReturn(mockApi);
        when(MediaType.parse("image/jpg")).thenReturn(mockImageMediaType);
        when(RequestBody.create(mockImageMediaType, image)).thenReturn(mockRequestBody);
        when(mockApi.syncEncounterForm(
                mockEncounterForm.getTokenAuthHeaderString(),
                mockEncounterForm.getEncounter().getId(),
                mockRequestBody)).thenReturn(mockEncounterCall);
        when(mockEncounterCall.execute()).thenReturn(mockEncounterSyncResponse);
        when(mockEncounterSyncResponse.isSuccessful()).thenReturn(false);
        whenNew(File.class).withAnyArguments().thenReturn(mockFile);

        syncService.syncEncounterForms(encounterFormsList);

        verify(mockFile, never()).delete();
        verify(mockEncounterForm, never()).setSynced();
        verifyStatic(never());
        EncounterFormDao.update(mockEncounterForm);
        verifyStatic();
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class),
                anyMapOf(String.class, String.class));
    }

    @Test
    public void syncMembers_twoMembers_succeeds() throws Exception {
        SyncService spiedService = spy(syncService);
        Member mockNewMember = mock(Member.class);
        when(mockNewMember.isNew()).thenReturn(true);
        Member mockExistingMember = mock(Member.class);
        when(mockExistingMember.isNew()).thenReturn(false);
        List<Member> membersList = new ArrayList<>();
        membersList.add(mockNewMember);
        membersList.add(mockExistingMember);

        doNothing().when(spiedService).syncMember(any(Member.class));

        spiedService.syncMembers(membersList);

        verify(spiedService, times(1)).syncMember(mockNewMember);
        verify(spiedService, times(1)).syncMember(mockExistingMember);
    }

    @Test
    public void syncMember_dirtyMember_succeeds() throws Exception {
        Member mockMember = mock(Member.class);
        SyncService spiedService = spy(syncService);

        doReturn(mockMemberSyncResponse).when(spiedService).sendSyncMemberRequest(mockMember);
        when(mockMember.isDirty()).thenReturn(false);
        doNothing().when(mockMember).updatePhotosFromSuccessfulSyncResponse(mockMemberSyncResponse);
        spiedService.syncMember(mockMember);

        verify(mockMember, times(1)).setSynced();
        verifyStatic();
        MemberDao.update(mockMember);
    }

    @Test
    public void syncMember_nonDirtyMember_succeeds() throws Exception {
        Member mockMember = mock(Member.class);
        SyncService spiedService = spy(syncService);

        doReturn(mockMemberSyncResponse).when(spiedService).sendSyncMemberRequest(mockMember);
        when(mockMember.isDirty()).thenReturn(true);
        doNothing().when(mockMember).updatePhotosFromSuccessfulSyncResponse(mockMemberSyncResponse);
        spiedService.syncMember(mockMember);

        verify(mockMember, never()).setSynced();

        verifyStatic();
        MemberDao.update(mockMember);
    }

    @Test
    public void syncMember_nullResponse_succeeds() throws Exception {
        Member mockMember = mock(Member.class);
        SyncService spiedService = spy(syncService);

        doReturn(null).when(spiedService).sendSyncMemberRequest(mockMember);
        spiedService.syncMember(mockMember);

        verify(mockMember, never()).isDirty();
        verify(mockMember, never()).updatePhotosFromSuccessfulSyncResponse(null);
        verify(mockMember, never()).setSynced();
        verifyStatic(never());
        MemberDao.update(mockMember);
    }

    @Test
    public void sendSyncMemberRequest_newMember_succeeds() throws Exception {
        Member mockMember = mock(Member.class);
        SyncService spiedService = spy(syncService);

        doReturn(true).when(mockMember).isNew();
        when(ApiService.requestBuilder(spiedService)).thenReturn(mockApi);
        when(mockMember.formatPostRequest(spiedService)).thenReturn(mockRequestBodyMap);
        when(mockApi.enrollMember(
                mockMember.getTokenAuthHeaderString(), mockRequestBodyMap))
                .thenReturn(mockMemberCall);
        when(mockMemberCall.execute()).thenReturn(mockMemberSyncResponse);
        when(mockMemberSyncResponse.isSuccessful()).thenReturn(true);

        Response<Member> response = spiedService.sendSyncMemberRequest(mockMember);

        assertTrue(response.isSuccessful());
        verifyStatic(never());
        ExceptionManager.reportException(any(Exception.class));
        ExceptionManager.requestFailure(
                anyString(), any(Request.class),
                any(okhttp3.Response.class), anyMapOf(String.class, String.class));
    }

    @Test
    public void sendSyncMemberRequest_existingMember_succeeds() throws Exception {
        Member mockMember = mock(Member.class);
        SyncService spiedService = spy(syncService);

        doReturn(false).when(mockMember).isNew();
        when(ApiService.requestBuilder(spiedService)).thenReturn(mockApi);
        when(mockMember.formatPatchRequest(spiedService)).thenReturn(mockRequestBodyMap);
        when(mockApi.syncMember(
                mockMember.getTokenAuthHeaderString(), mockMember.getId(), mockRequestBodyMap))
                .thenReturn(mockMemberCall);
        when(mockMemberCall.execute()).thenReturn(mockMemberSyncResponse);
        when(mockMemberSyncResponse.isSuccessful()).thenReturn(true);

        Response<Member> response = spiedService.sendSyncMemberRequest(mockMember);

        assertTrue(response.isSuccessful());
        verify(mockApi, times(1)).syncMember(mockMember.getTokenAuthHeaderString(),  mockMember.getId(), mockRequestBodyMap);
        verifyStatic(never());
        ExceptionManager.reportException(any(Exception.class));
        ExceptionManager.requestFailure(
                anyString(), any(Request.class),
                any(okhttp3.Response.class), anyMapOf(String.class, String.class));
    }

    // LEFT TODO: Test if response is unsuccessful.
}
