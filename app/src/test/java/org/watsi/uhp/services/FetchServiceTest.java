package org.watsi.uhp.services;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;

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
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.PreferencesManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import okhttp3.Headers;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
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
@PrepareForTest({ AccountManager.class, ApiService.class, BillableDao.class, ExceptionManager.class,
        FetchService.class, Headers.class, MemberDao.class, Request.class, okhttp3.Response.class,
        Response.class, SessionManager.class })
public class FetchServiceTest {

    @Mock
    PreferencesManager mockPreferencesManager;
    @Mock
    UhpApi mockApi;
    @Mock
    Call<List<Member>> mockFetchMembersCall;
    @Mock
    Response<List<Member>> mockFetchMembersResponse;
    @Mock
    List<Member> mockMembersList;
    @Mock
    HashMap<String, String> mockParamMap;
    @Mock
    Call<List<Billable>> mockFetchBillablesCall;
    @Mock
    Response<List<Billable>> mockFetchBillablesResponse;
    @Mock
    List<Billable> mockBillablesList;
    @Mock
    AccountManager mockAccountManager;
    @Mock
    SessionManager mockSessionManager;
    @Mock
    AccountManagerFuture<Bundle> mockTokenFuture;
    @Mock
    Bundle mockBundle;

    private FetchService fetchService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(AccountManager.class);
        mockStatic(ApiService.class);
        mockStatic(BillableDao.class);
        mockStatic(ExceptionManager.class);
        mockStatic(MemberDao.class);
        fetchService = new FetchService();
    }

    @Test
    public void performSync_nullAccountManagerFuture() throws Exception {
        FetchService spiedFetchService = spy(fetchService);

        when(AccountManager.get(spiedFetchService)).thenReturn(mockAccountManager);
        whenNew(PreferencesManager.class).withAnyArguments().thenReturn(mockPreferencesManager);
        whenNew(SessionManager.class).withArguments(mockPreferencesManager, mockAccountManager)
                .thenReturn(mockSessionManager);
        when(mockSessionManager.fetchToken()).thenReturn(null);

        boolean result = spiedFetchService.performSync();

        assertTrue(result);
        verify(spiedFetchService, never()).fetchMembers(anyString(), any(PreferencesManager.class));
        verify(spiedFetchService, never())
                .fetchBillables(anyString(), any(PreferencesManager.class));
        verifyStatic(never());
        ExceptionManager.reportException(any(Exception.class));
    }

    private void mockTokenFetch(String token, FetchService service) throws Exception {
        when(AccountManager.get(service)).thenReturn(mockAccountManager);
        whenNew(SessionManager.class).withArguments(mockPreferencesManager, mockAccountManager)
                .thenReturn(mockSessionManager);
        when(mockSessionManager.fetchToken()).thenReturn(mockTokenFuture);
        when(mockTokenFuture.getResult()).thenReturn(mockBundle);
        when(mockBundle.getString(AccountManager.KEY_AUTHTOKEN)).thenReturn(token);
    }

    @Test
    public void performSync_nullAuthToken() throws Exception {
        FetchService spiedFetchService = spy(fetchService);

        mockTokenFetch(null, spiedFetchService);
        whenNew(PreferencesManager.class).withAnyArguments().thenReturn(mockPreferencesManager);

        boolean result = spiedFetchService.performSync();

        assertTrue(result);
        verify(spiedFetchService, never()).fetchMembers(anyString(), any(PreferencesManager.class));
        verify(spiedFetchService, never())
                .fetchBillables(anyString(), any(PreferencesManager.class));
        verifyStatic(never());
        ExceptionManager.reportException(any(Exception.class));
    }

    @Test
    public void performSync_hasAuthToken_fetchDoesNotThrowException() throws Exception {
        String token = "token";
        FetchService spiedFetchService = spy(fetchService);

        mockTokenFetch(token, spiedFetchService);
        doNothing().when(spiedFetchService).fetchMembers(token, mockPreferencesManager);
        doNothing().when(spiedFetchService).fetchBillables(token, mockPreferencesManager);
        whenNew(PreferencesManager.class).withAnyArguments().thenReturn(mockPreferencesManager);

        boolean result = spiedFetchService.performSync();

        assertTrue(result);
        verify(spiedFetchService, times(1)).fetchMembers(token, mockPreferencesManager);
        verify(spiedFetchService, times(1)).fetchBillables(token, mockPreferencesManager);
    }

    @Test
    public void performSync_hasAuthToken_fetchThrowsException() throws Exception {
        String token = "token";
        FetchService spiedFetchService = spy(fetchService);
        SQLException mockException = mock(SQLException.class);

        mockTokenFetch(token, spiedFetchService);
        doThrow(mockException).when(spiedFetchService)
                .fetchMembers(token, mockPreferencesManager);
        whenNew(PreferencesManager.class).withAnyArguments().thenReturn(mockPreferencesManager);

        boolean result = spiedFetchService.performSync();

        assertFalse(result);
        verifyStatic(times(1));
        ExceptionManager.reportException(mockException);
    }

    private Response mockMembersApiRequest(String token, FetchService spiedFetchService)
            throws Exception {
        String lastModifiedTimestamp = "foo";

        doNothing().when(spiedFetchService).notifyAboutMembersToBeDeleted(anyListOf(Member.class));
        doNothing().when(spiedFetchService).createOrUpdateMembers(anyListOf(Member.class));
        when(mockPreferencesManager.getMemberLastModified()).thenReturn(lastModifiedTimestamp);
        when(ApiService.requestBuilder(spiedFetchService)).thenReturn(mockApi);
        when(mockApi.members(token, lastModifiedTimestamp, BuildConfig.PROVIDER_ID))
                .thenReturn(mockFetchMembersCall);
        when(mockFetchMembersCall.execute()).thenReturn(mockFetchMembersResponse);
        doNothing().when(mockPreferencesManager).setMemberLastModified(anyString());
        return mockFetchMembersResponse;
    }

    @Test
    public void fetchMembers_successfulResponse() throws Exception {
        FetchService spiedFetchService = spy(FetchService.class);
        String token = "token";
        String updatedLastModifiedTimestamp = "bar";

        Response mockResponse = mockMembersApiRequest(token, spiedFetchService);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockMembersList);
        Headers mockHeaders = mock(Headers.class);
        when(mockResponse.headers()).thenReturn(mockHeaders);
        when(mockHeaders.get("last-modified")).thenReturn(updatedLastModifiedTimestamp);

        spiedFetchService.fetchMembers(token, mockPreferencesManager);

        verify(spiedFetchService, times(1)).notifyAboutMembersToBeDeleted(mockMembersList);
        verify(spiedFetchService, times(1)).createOrUpdateMembers(mockMembersList);
        verify(mockPreferencesManager, times(1))
                .setMemberLastModified(updatedLastModifiedTimestamp);
    }

    @Test
    public void fetchMembers_unsuccessfulResponse_304response() throws Exception {
        FetchService spiedFetchService = spy(FetchService.class);
        String token = "token";
        int responseCode = 304;

        Response mockResponse = mockMembersApiRequest(token, spiedFetchService);
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.code()).thenReturn(responseCode);

        spiedFetchService.fetchMembers(token, mockPreferencesManager);

        verify(spiedFetchService, never()).notifyAboutMembersToBeDeleted(anyListOf(Member.class));
        verify(spiedFetchService, never()).createOrUpdateMembers(anyListOf(Member.class));
        verify(mockPreferencesManager, never()).setMemberLastModified(anyString());
        verifyStatic(never());
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class));
    }

    @Test
    public void fetchMembers_unsuccessfulResponse_non304response() throws Exception {
        FetchService spiedFetchService = spy(FetchService.class);
        String token = "token";
        int responseCode = 500;

        Response mockResponse = mockMembersApiRequest(token, spiedFetchService);
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.code()).thenReturn(responseCode);
        Request mockRawRequest = mock(Request.class);
        okhttp3.Response mockRawResponse = mock(okhttp3.Response.class);
        when(mockFetchMembersCall.request()).thenReturn(mockRawRequest);
        when(mockResponse.raw()).thenReturn(mockRawResponse);

        spiedFetchService.fetchMembers(token, mockPreferencesManager);

        verify(spiedFetchService, never()).notifyAboutMembersToBeDeleted(anyListOf(Member.class));
        verify(spiedFetchService, never()).createOrUpdateMembers(anyListOf(Member.class));
        verify(mockPreferencesManager, never()).setMemberLastModified(anyString());
        verifyStatic();
        ExceptionManager.requestFailure(
                "Failed to fetch members", mockRawRequest, mockRawResponse);
    }

    private Member mockMember(boolean synced) throws Exception {
        Member mockedMember = mock(Member.class);
        when(mockedMember.getId()).thenReturn(UUID.randomUUID());
        when(mockedMember.isSynced()).thenReturn(synced);
        doNothing().when(mockedMember).setSynced();
        return mockedMember;
    }

    @Test
    public void notifyAboutMembersToBeDeleted() throws Exception {
        Member newMemberOnBackend = mockMember(false);
        Member localOnlySyncedMember = mockMember(true);
        Member unSyncedMember = mockMember(false);
        List<Member> fetchedMembers = new ArrayList<>();
        fetchedMembers.add(newMemberOnBackend);
        Set<UUID> localMemberIds = new HashSet<>();
        localMemberIds.add(localOnlySyncedMember.getId());
        localMemberIds.add(unSyncedMember.getId());

        when(MemberDao.allMemberIds()).thenReturn(localMemberIds);
        when(MemberDao.findById(localOnlySyncedMember.getId())).thenReturn(localOnlySyncedMember);
        when(MemberDao.findById(unSyncedMember.getId())).thenReturn(unSyncedMember);
        whenNew(HashMap.class).withNoArguments().thenReturn(mockParamMap);
        when(mockParamMap.put(anyString(), anyString())).thenReturn(null);

        fetchService.notifyAboutMembersToBeDeleted(fetchedMembers);

        verify(mockParamMap, times(1)).put("member.id", localOnlySyncedMember.getId().toString());
        verify(mockParamMap, never()).put("member.id", newMemberOnBackend.getId().toString());
        verify(mockParamMap, never()).put("member.id", unSyncedMember.getId().toString());
        verifyStatic();
        ExceptionManager.reportMessage(
                "Member synced on device but not in backend", "warning", mockParamMap);
    }

    @Test
    public void createOrUpdateMembers_unPersistedMember_syncsAndCreates() throws Exception {
        Member unPersistedMember = mockMember(false);
        List<Member> fetchedMembers = new ArrayList<>();
        fetchedMembers.add(unPersistedMember);

        fetchService.createOrUpdateMembers(fetchedMembers);

        verify(unPersistedMember, times(1)).setSynced();
        verify(unPersistedMember, never()).setPhoto(any(byte[].class));
        verifyStatic();
        MemberDao.createOrUpdate(unPersistedMember);
    }

    @Test
    public void createOrUpdateMembers_unSyncedPersistedMember_doesNotSyncOrUpdate() throws Exception {
        Member unSyncedPersistedMember = mockMember(false);
        List<Member> fetchedMembers = new ArrayList<>();
        fetchedMembers.add(unSyncedPersistedMember);

        when(MemberDao.findById(unSyncedPersistedMember.getId()))
                .thenReturn(unSyncedPersistedMember);

        fetchService.createOrUpdateMembers(fetchedMembers);

        verify(unSyncedPersistedMember, never()).setSynced();
        verify(unSyncedPersistedMember, never()).setPhoto(any(byte[].class));
        verifyStatic(never());
        MemberDao.createOrUpdate(unSyncedPersistedMember);
    }

    @Test
    public void createOrUpdateMembers_syncedPersistedMemberWithNewPhoto_syncsAndUpdatesButDoesNotCopyPhoto() throws Exception {
        Member syncedPersistedMemberWithNewPhoto = mockMember(true);
        List<Member> fetchedMembers = new ArrayList<>();
        fetchedMembers.add(syncedPersistedMemberWithNewPhoto);

        when(MemberDao.findById(syncedPersistedMemberWithNewPhoto.getId()))
                .thenReturn(syncedPersistedMemberWithNewPhoto);

        fetchService.createOrUpdateMembers(fetchedMembers);

        verify(syncedPersistedMemberWithNewPhoto, times(1)).setSynced();
        verify(syncedPersistedMemberWithNewPhoto, never()).setPhoto(any(byte[].class));
        verifyStatic();
        MemberDao.createOrUpdate(syncedPersistedMemberWithNewPhoto);
    }

    @Test
    public void createOrUpdateMembers_syncedPersistedMemberWithSamePhoto_syncsAndUpdatesAndCopiesPhoto() throws Exception {
        Member syncedPersistedMemberWithSamePhoto = mockMember(true);
        List<Member> fetchedMembers = new ArrayList<>();
        fetchedMembers.add(syncedPersistedMemberWithSamePhoto);
        byte[] photo = new byte[]{(byte)0xe0};

        when(syncedPersistedMemberWithSamePhoto.getPhoto()).thenReturn(photo);
        when(syncedPersistedMemberWithSamePhoto.getPhotoUrl()).thenReturn("foo");

        when(MemberDao.findById(syncedPersistedMemberWithSamePhoto.getId()))
                .thenReturn(syncedPersistedMemberWithSamePhoto);

        fetchService.createOrUpdateMembers(fetchedMembers);

        verify(syncedPersistedMemberWithSamePhoto, times(1)).setSynced();
        verify(syncedPersistedMemberWithSamePhoto, times(1)).setPhoto(photo);
        verifyStatic();
        MemberDao.createOrUpdate(syncedPersistedMemberWithSamePhoto);
    }

    private Response mockBillablesApiRequest(FetchService spiedFetchService) throws Exception {
        String lastModifiedTimestamp = "foo";
        String token = "token";

        when(mockPreferencesManager.getBillablesLastModified()).thenReturn(lastModifiedTimestamp);
        when(ApiService.requestBuilder(spiedFetchService)).thenReturn(mockApi);
        when(mockApi.billables(token, lastModifiedTimestamp, BuildConfig.PROVIDER_ID))
                .thenReturn(mockFetchBillablesCall);
        when(mockFetchBillablesCall.execute()).thenReturn(mockFetchBillablesResponse);
        doNothing().when(mockPreferencesManager).setBillablesLastModified(anyString());
        return mockFetchBillablesResponse;
    }

    @Test
    public void fetchBillables_successfulResponse() throws Exception {
        FetchService spiedFetchService = spy(FetchService.class);
        String updatedLastModifiedTimestamp = "bar";
        String token = "token";

        Response mockResponse = mockBillablesApiRequest(spiedFetchService);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockBillablesList);
        Headers mockHeaders = mock(Headers.class);
        when(mockResponse.headers()).thenReturn(mockHeaders);
        when(mockHeaders.get("last-modified")).thenReturn(updatedLastModifiedTimestamp);

        spiedFetchService.fetchBillables(token, mockPreferencesManager);

        verifyStatic();
        BillableDao.clear();
        verifyStatic();
        BillableDao.create(mockBillablesList);
        verify(mockPreferencesManager, times(1))
                .setBillablesLastModified(updatedLastModifiedTimestamp);
    }

    @Test
    public void fetchBillablesData_unsuccessfulResponse_304response() throws Exception {
        FetchService spiedFetchService = spy(FetchService.class);
        String token = "token";
        int responseCode = 304;

        Response mockResponse = mockBillablesApiRequest(spiedFetchService);
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.code()).thenReturn(responseCode);

        spiedFetchService.fetchBillables(token, mockPreferencesManager);

        verifyStatic(never());
        BillableDao.clear();
        verifyStatic(never());
        BillableDao.create(anyListOf(Billable.class));
        verify(mockPreferencesManager, never()).setBillablesLastModified(anyString());
        verifyStatic(never());
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class));
    }

    @Test
    public void fetchBillablesData_unsuccessfulResponse_non304response() throws Exception {
        FetchService spiedFetchService = spy(FetchService.class);
        String token = "token";
        int responseCode = 500;

        Response mockResponse = mockBillablesApiRequest(spiedFetchService);
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.code()).thenReturn(responseCode);
        Request mockRawRequest = mock(Request.class);
        okhttp3.Response mockRawResponse = mock(okhttp3.Response.class);
        when(mockFetchBillablesCall.request()).thenReturn(mockRawRequest);
        when(mockResponse.raw()).thenReturn(mockRawResponse);

        spiedFetchService.fetchBillables(token, mockPreferencesManager);

        verifyStatic(never());
        BillableDao.clear();
        verifyStatic(never());
        BillableDao.create(anyListOf(Billable.class));
        verify(mockPreferencesManager, never()).setBillablesLastModified(anyString());
        verifyStatic();
        ExceptionManager.requestFailure(
                "Failed to fetch billables", mockRawRequest, mockRawResponse);
    }
}
