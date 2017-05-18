package org.watsi.uhp.models;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.common.io.ByteStreams;
import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.api.UhpApi;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.FileManager;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Response;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ApiService.class, Bitmap.class, BitmapFactory.class, ByteStreams.class,
        EncounterDao.class, File.class, FileManager.class, ExceptionManager.class,
        MediaStore.Images.Media.class, Member.class, MemberDao.class, okhttp3.Response.class,
        Request.class, Response.class, ResponseBody.class, Uri.class})
public class MemberTest {
    private final String localPhotoUrl = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
    private final String remotePhotoUrl = "https://d2bxcwowl6jlve.cloudfront.net/media/foo-3bf77f20d8119074";
    private final String localNationalIdPhotoUrl = "content://org.watsi.uhp.fileprovider/captured_image/national_id.jpg";

    private Member member;

    @Mock
    Response<Member> mockSyncResponse;
    @Mock
    UhpApi mockApi;
    @Mock
    Response<Member> mockMemberSyncResponse;
    @Mock
    HashMap<String, RequestBody> mockRequestBodyMap;
    @Mock
    Call<Member> mockMemberCall;
    @Mock
    Context mockContext;
    @Mock
    Request.Builder mockRequestBuilder;
    @Mock
    Request mockRequest;
    @Mock
    OkHttpClient mockHttpClient;
    @Mock
    okhttp3.Response mockResponse;
    @Mock
    okhttp3.Call mockCall;
    @Mock
    InputStream mockInputStream;
    @Mock
    Dao mockDao;
    @Mock
    Uri mockUri;

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(ApiService.class);
        mockStatic(Bitmap.class);
        mockStatic(BitmapFactory.class);
        mockStatic(ByteStreams.class);
        mockStatic(ExceptionManager.class);
        mockStatic(FileManager.class);
        mockStatic(MediaStore.Images.Media.class);
        mockStatic(MemberDao.class);
        mockStatic(Uri.class);
        member = new Member();
    }

    @Test
    public void setPhoneNumber_isNull_setsPhoneNumberToNull() throws Exception {
        member.setPhoneNumber(null);

        assertEquals(member.getPhoneNumber(), null);
    }

    @Test
    public void setPhoneNumber_isValid_setsPhoneNumber() throws Exception {
        mockStatic(Member.class);
        when(Member.validPhoneNumber(anyString())).thenReturn(true);

        member.setPhoneNumber("0777555555");

        assertEquals(member.getPhoneNumber(), "0777555555");
    }

    @Test(expected=AbstractModel.ValidationException.class)
    public void setPhoneNumber_isInvalid_throwsException() throws Exception {
        mockStatic(Member.class);
        when(Member.validPhoneNumber(anyString())).thenReturn(false);

        member.setPhoneNumber("");
    }

    @Test
    public void handleUpdateFromSync_responseHasPhotoUrl_setsAndFetchesPhoto() throws Exception {
        String previousPhotoUrl = "prevUrl";
        member.setPhotoUrl(previousPhotoUrl);
        Member memberSpy = spy(member);
        Member responseMember = new Member();
        responseMember.setPhotoUrl(remotePhotoUrl);

        doNothing().when(memberSpy).fetchAndSetPhotoFromUrl();
        when(mockMemberSyncResponse.body()).thenReturn(responseMember);
        when(FileManager.isLocal(previousPhotoUrl)).thenReturn(false);

        memberSpy.handleUpdateFromSync(mockMemberSyncResponse);

        verify(memberSpy, times(1)).fetchAndSetPhotoFromUrl();
        assertEquals(memberSpy.getPhotoUrl(), remotePhotoUrl);
    }

    @Test
    public void handleUpdateFromSync_responseHasPhotoUrlAndExistingPhotoIsLocal_deletesLocalPhoto() throws Exception {
        String previousPhotoUrl = "prevUrl";
        member.setPhotoUrl(previousPhotoUrl);
        Member memberSpy = spy(member);
        Member responseMember = new Member();
        responseMember.setPhotoUrl(remotePhotoUrl);

        doNothing().when(memberSpy).fetchAndSetPhotoFromUrl();
        when(mockMemberSyncResponse.body()).thenReturn(responseMember);
        when(FileManager.isLocal(previousPhotoUrl)).thenReturn(true);

        memberSpy.handleUpdateFromSync(mockMemberSyncResponse);

        verifyStatic();
        FileManager.deleteLocalPhoto(previousPhotoUrl);
    }

    @Test
    public void handleUpdateFromSync_responseHasNationalIdPhotoUrl_setsAndDeletesExisting() throws Exception {
        String previousPhotoUrl = "prevUrl";
        member.setNationalIdPhotoUrl(previousPhotoUrl);
        Member memberSpy = spy(member);
        Member responseMember = new Member();
        responseMember.setNationalIdPhotoUrl(remotePhotoUrl);

        when(mockMemberSyncResponse.body()).thenReturn(responseMember);
        when(FileManager.isLocal(previousPhotoUrl)).thenReturn(true);

        memberSpy.handleUpdateFromSync(mockMemberSyncResponse);

        assertEquals(memberSpy.getNationalIdPhotoUrl(), remotePhotoUrl);
        verifyStatic();
        FileManager.deleteLocalPhoto(previousPhotoUrl);
    }

    @Test
    public void updateFromFetch_noPersistedMember_createsMember() throws Exception {
        UUID id = UUID.randomUUID();
        member.setId(id);
        Member memberSpy = spy(member);

        when(MemberDao.findById(id)).thenReturn(null);
        doReturn(mockDao).when(memberSpy).getDao();

        memberSpy.updateFromFetch();

        verify(mockDao, times(1)).createOrUpdate(memberSpy);
        verify(memberSpy, never()).setPhoto(any(byte[].class));
    }

    @Test
    public void updateFromFetch_persistedMemberNotSynced_doesNotUpdate() throws Exception {
        member.setId(UUID.randomUUID());
        Member memberSpy = spy(member);
        Member persistedMember = mock(Member.class);

        when(persistedMember.isSynced()).thenReturn(false);
        when(MemberDao.findById(memberSpy.getId())).thenReturn(persistedMember);
        doReturn(mockDao).when(memberSpy).getDao();

        memberSpy.updateFromFetch();

        verify(mockDao, never()).createOrUpdate(memberSpy);
        verify(memberSpy, never()).setPhoto(any(byte[].class));
    }

    @Test
    public void updateFromFetch_persistedMemberSyncedSamePhoto_setsPhotoAndUpdates() throws Exception {
        member.setId(UUID.randomUUID());
        member.setPhotoUrl(remotePhotoUrl);
        Member memberSpy = spy(member);
        byte[] photoBytes = new byte[]{(byte)0xe0};
        Member persistedMember = new Member();
        persistedMember.setPhoto(photoBytes);
        persistedMember.setPhotoUrl(remotePhotoUrl);
        Member persistedMemberSpy = spy(persistedMember);

        when(persistedMemberSpy.isSynced()).thenReturn(true);
        when(MemberDao.findById(memberSpy.getId())).thenReturn(persistedMemberSpy);
        doReturn(mockDao).when(memberSpy).getDao();

        memberSpy.updateFromFetch();

        verify(mockDao, times(1)).createOrUpdate(memberSpy);
        verify(memberSpy, times(1)).setPhoto(photoBytes);
    }

    @Test
    public void postApiCall() throws Exception {
        member.setToken("foo");
        member.setId(UUID.randomUUID());
        Member memberSpy = spy(member);

        doReturn(mockRequestBodyMap).when(memberSpy).formatPostRequest(mockContext);
        when(ApiService.requestBuilder(mockContext)).thenReturn(mockApi);

        memberSpy.postApiCall(mockContext);

        verify(mockApi).enrollMember("Token foo", mockRequestBodyMap);
    }

    @Test
    public void patchApiCall() throws Exception {
        member.setToken("foo");
        member.setId(UUID.randomUUID());
        Member memberSpy = spy(member);

        doReturn(mockRequestBodyMap).when(memberSpy).formatPatchRequest(mockContext);
        when(ApiService.requestBuilder(mockContext)).thenReturn(mockApi);

        memberSpy.patchApiCall(mockContext);

        verify(mockApi).syncMember("Token foo", member.getId(), mockRequestBodyMap);
    }

    @Test
    public void fetchAndSetPhotoFromUrl_localUrl_doesNotAttemptToFetch() throws Exception {
        member.setPhotoUrl(localPhotoUrl);

        whenNew(Request.Builder.class).withNoArguments().thenReturn(mockRequestBuilder);
        when(FileManager.isLocal(localPhotoUrl)).thenReturn(true);
        when(mockRequestBuilder.url(localPhotoUrl)).thenReturn(mockRequestBuilder);

        member.fetchAndSetPhotoFromUrl();

        verify(mockRequestBuilder, never()).build();
    }

    private void mockPhotoFetch() throws Exception {
        whenNew(Request.Builder.class).withNoArguments().thenReturn(mockRequestBuilder);
        when(FileManager.isLocal(remotePhotoUrl)).thenReturn(false);
        when(mockRequestBuilder.url(remotePhotoUrl)).thenReturn(mockRequestBuilder);
        when(mockRequestBuilder.build()).thenReturn(mockRequest);
        whenNew(OkHttpClient.class).withNoArguments().thenReturn(mockHttpClient);
        when(mockHttpClient.newCall(mockRequest)).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
    }

    @Test
    public void fetchAndSetPhotoFromUrl_remoteUrlFetchSucceeds_setsPhoto() throws Exception {
        member.setPhotoUrl(remotePhotoUrl);
        byte[] photoBytes = new byte[]{(byte)0xe0};
        // mock with PowerMockito because we want to mock byteStream which is a final method
        ResponseBody mockResponseBody = PowerMockito.mock(ResponseBody.class);

        mockPhotoFetch();
        when(mockResponse.isSuccessful()).thenReturn(true);
        doReturn(mockResponseBody).when(mockResponse).body();
        when(mockResponse.body()).thenReturn(mockResponseBody);
        PowerMockito.when(mockResponseBody.byteStream()).thenReturn(mockInputStream);
        when(ByteStreams.toByteArray(mockInputStream)).thenReturn(photoBytes);

        member.fetchAndSetPhotoFromUrl();

        assertEquals(member.getPhoto(), photoBytes);
        verify(mockResponse, times(1)).close();
    }

    @Test
    public void fetchAndSetPhotoFromUrl_remoteUrlFetchFails_reportsFailure() throws Exception {
        member.setId(UUID.randomUUID());
        member.setPhotoUrl(remotePhotoUrl);
        member.setPhoto(null);

        mockPhotoFetch();
        when(mockResponse.isSuccessful()).thenReturn(false);

        member.fetchAndSetPhotoFromUrl();

        assertNull(member.getPhoto());
        verifyStatic();
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class),
                anyMapOf(String.class, String.class));
    }

    @Test
    public void getPhotoBitmap_photoIsNullAndPhotoUrlIsNull() throws Exception {
        ContentResolver mockContentResolver = mock(ContentResolver.class);

        assertNull(member.getPhotoBitmap(mockContentResolver));
    }

    @Test
    public void getPhotoBitmap_photoIsNullAndPhotoUrlIsNotLocalUrl() throws Exception {
        member.setPhotoUrl(remotePhotoUrl);
        ContentResolver mockContentResolver = mock(ContentResolver.class);

        when(FileManager.isLocal(member.getPhotoUrl())).thenReturn(false);

        assertNull(member.getPhotoBitmap(mockContentResolver));
    }

    @Test
    public void getPhotoBitmap_photoIsNullButLocalPhotoUrl() throws Exception {
        member.setPhotoUrl(localPhotoUrl);
        Bitmap mockBitmap = mock(Bitmap.class);
        ContentResolver mockContentResolver = mock(ContentResolver.class);

        when(FileManager.isLocal(member.getPhotoUrl())).thenReturn(true);
        when(Uri.parse(member.getPhotoUrl())).thenReturn(mockUri);
        when(MediaStore.Images.Media.getBitmap(mockContentResolver, mockUri)).thenReturn(mockBitmap);

        assertEquals(member.getPhotoBitmap(mockContentResolver), mockBitmap);
    }

    @Test
    public void getPhotoBitmap_photoIsNotNull() throws Exception {
        ContentResolver mockContentResolver = mock(ContentResolver.class);
        byte[] photoBytes = new byte[]{};
        member.setPhoto(photoBytes);
        Bitmap bitmap = mock(Bitmap.class);

        when(Bitmap.createBitmap(any(Bitmap.class))).thenReturn(bitmap);
        when(BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length)).thenReturn(bitmap);

        assertEquals(member.getPhotoBitmap(mockContentResolver), bitmap);
    }

    @Test
    public void shouldCaptureFingerprint() throws Exception {
        member.setAge(Member.MINIMUM_FINGERPRINT_AGE - 1);
        assertFalse(member.shouldCaptureFingerprint());

        member.setAge(Member.MINIMUM_FINGERPRINT_AGE);
        assertTrue(member.shouldCaptureFingerprint());
    }

    @Test
    public void shouldCaptureNationalIdPhoto() throws Exception {
        member.setAge(Member.MINIMUM_NATIONAL_ID_AGE - 1);
        assertFalse(member.shouldCaptureNationalIdPhoto());

        member.setAge(Member.MINIMUM_NATIONAL_ID_AGE);
        assertTrue(member.shouldCaptureNationalIdPhoto());
    }

    @Test
    public void validPhoneNumber() throws Exception {
        assertFalse(Member.validPhoneNumber(null));
        assertFalse(Member.validPhoneNumber(""));
        assertFalse(Member.validPhoneNumber("123"));
        assertFalse(Member.validPhoneNumber("001234567"));
        assertFalse(Member.validPhoneNumber("1234567891"));

        assertTrue(Member.validPhoneNumber("0734567894"));
        assertTrue(Member.validPhoneNumber("773041232"));
    }

    @Test
    public void getFormattedPhoneNumber() throws Exception {
        member.setPhoneNumber(null);
        assertNull(member.getFormattedPhoneNumber());

        member.setPhoneNumber("0123456789");
        assertEquals(member.getFormattedPhoneNumber(), "(0) 123 456 789");

        member.setPhoneNumber("123456789");
        assertEquals(member.getFormattedPhoneNumber(), "(0) 123 456 789");
    }

    @Test
    public void getFormattedCardId() throws Exception {
        member.setCardId("RWI123456");
        assertEquals(member.getFormattedCardId(), "RWI 123 456");
    }

    @Test
    public void getFormattedAgeAndGender() {
        member.setAge(0);
        member.setGender(Member.GenderEnum.F);
        assertEquals(member.getFormattedAgeAndGender(), "0 years / F");

        member.setAge(1);
        member.setGender(Member.GenderEnum.M);
        assertEquals(member.getFormattedAgeAndGender(), "1 year / M");

        member.setAge(52);
        member.setGender(Member.GenderEnum.F);
        assertEquals(member.getFormattedAgeAndGender(), "52 years / F");
    }

    private Member mockNewborn() throws Exception {
        Member newborn = new Member();
        newborn.setId(UUID.randomUUID());
        newborn.setFullName("Akiiki Monday");
        newborn.setCardId("RWI111111");
        newborn.setBirthdate(Calendar.getInstance().getTime());
        newborn.setBirthdateAccuracy(Member.BirthdateAccuracyEnum.D);
        newborn.setGender(Member.GenderEnum.F);
        newborn.setHouseholdId(UUID.randomUUID());
        newborn.setEnrolledAt(Calendar.getInstance().getTime());
        return newborn;
    }

    @Test
    public void formatPostRequest_validMember_includesId() throws Exception {
        Member newborn = mockNewborn();

        Map<String, RequestBody> requestBodyMap = newborn.formatPostRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_ID).writeTo(buffer);
        assertEquals(buffer.readUtf8(), newborn.getId().toString());
    }

    @Test
    public void formatPostRequest_validMember_includesGender() throws Exception {
        Member newborn = mockNewborn();

        Map<String, RequestBody> requestBodyMap = newborn.formatPostRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_GENDER).writeTo(buffer);
        assertEquals(buffer.readUtf8(), "F");
        buffer.clear();
    }

    @Test
    public void formatPostRequest_validMember_includesName() throws Exception {
        Member newborn = mockNewborn();

        Map<String, RequestBody> requestBodyMap = newborn.formatPostRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_FULL_NAME).writeTo(buffer);
        assertEquals(buffer.readUtf8(), newborn.getFullName());
    }

    @Test
    public void formatPostRequest_validMember_includesCardId() throws Exception {
        Member newborn = mockNewborn();

        Map<String, RequestBody> requestBodyMap = newborn.formatPostRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_CARD_ID).writeTo(buffer);
        assertEquals(buffer.readUtf8(), newborn.getCardId());
    }

    @Test
    public void formatPostRequest_validMember_includesProviderDetails() throws Exception {
        Member newborn = mockNewborn();

        Map<String, RequestBody> requestBodyMap = newborn.formatPostRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get("provider_assignment[provider_id]").writeTo(buffer);
        assertEquals(buffer.readUtf8(), "1");
        buffer.clear();

        requestBodyMap.get("provider_assignment[start_reason]").writeTo(buffer);
        assertEquals(buffer.readUtf8(), "birth");
    }

    @Test
    public void formatPostRequest_validMember_includesBirthdayInfo() throws Exception {
        Member newborn = mockNewborn();

        Map<String, RequestBody> requestBodyMap = newborn.formatPostRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_BIRTHDATE_ACCURACY).writeTo(buffer);
        assertEquals(buffer.readUtf8(), "D");
        buffer.clear();

        requestBodyMap.get(Member.FIELD_NAME_BIRTHDATE).writeTo(buffer);
        assertEquals(buffer.readUtf8(), Clock.asIso(newborn.getBirthdate()));
    }

    @Test
    public void formatPostRequest_validMember_includesEnrolledAt() throws Exception {
        Member newborn = mockNewborn();

        Map<String, RequestBody> requestBodyMap = newborn.formatPostRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_ENROLLED_AT).writeTo(buffer);
        assertEquals(buffer.readUtf8(), Clock.asIso(newborn.getEnrolledAt()));
    }

    @Test
    public void formatPostRequest_validMember_clearsDirtyFields() throws Exception {
        Member newborn = spy(mockNewborn());

        newborn.formatPostRequest(mockContext);

        verify(newborn, times(1)).clearDirtyFields();
    }

    @Test
    public void formatPostRequest_validMemberNullPhotoUrl_doesNotIncludePhoto() throws Exception {
        Member newborn = spy(mockNewborn());
        newborn.setPhotoUrl(null);

        Map<String, RequestBody> requestBodyMap = newborn.formatPostRequest(mockContext);

        assertFalse(requestBodyMap.containsKey(Member.FIELD_NAME_PHOTO));
    }


    @Test
    public void formatPostRequest_validMemberHasPhotoUrl_includesPhoto() throws Exception {
        Member newborn = spy(mockNewborn());
        newborn.setPhotoUrl(localPhotoUrl);
        byte[] mockPhoto = new byte[]{(byte)0xe0};

        when(FileManager.isLocal(localPhotoUrl)).thenReturn(true);
        when(Uri.parse(localPhotoUrl)).thenReturn(mockUri);
        when(FileManager.readFromUri(mockUri, mockContext)).thenReturn(mockPhoto);

        Map<String, RequestBody> requestBodyMap = newborn.formatPostRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_PHOTO).writeTo(buffer);
        assertTrue(Arrays.equals(buffer.readByteArray(), mockPhoto));
    }

    @Test
    public void formatPatchRequest_dirtyPhoto_includesPhoto() throws Exception {
        Member editedMember = spy(member);
        editedMember.setPhotoUrl(localPhotoUrl);
        byte[] mockPhoto = new byte[]{(byte)0xe0};

        when(editedMember.dirty(Member.FIELD_NAME_PHOTO)).thenReturn(true);
        when(FileManager.isLocal(localPhotoUrl)).thenReturn(true);
        when(Uri.parse(localPhotoUrl)).thenReturn(mockUri);
        when(FileManager.readFromUri(mockUri, mockContext)).thenReturn(mockPhoto);

        Map<String, RequestBody> requestBodyMap = editedMember.formatPatchRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_PHOTO).writeTo(buffer);
        assertTrue(Arrays.equals(buffer.readByteArray(), mockPhoto));
    }

    @Test
    public void formatPatchRequest_dirtyPhotoAndNationalId_doesNotIncludeNationalIdPhoto() throws Exception {
        member.setPhotoUrl(localPhotoUrl);
        member.setNationalIdPhotoUrl(localNationalIdPhotoUrl);
        Member editedMember = spy(member);
        byte[] mockPhoto = new byte[]{(byte)0xe0};

        when(editedMember.dirty(Member.FIELD_NAME_PHOTO)).thenReturn(true);
        when(editedMember.dirty(Member.FIELD_NAME_NATIONAL_ID_PHOTO)).thenReturn(true);
        when(FileManager.isLocal(localPhotoUrl)).thenReturn(true);
        when(FileManager.isLocal(localNationalIdPhotoUrl)).thenReturn(true);
        when(Uri.parse(localPhotoUrl)).thenReturn(mockUri);
        when(Uri.parse(localNationalIdPhotoUrl)).thenReturn(mockUri);
        when(FileManager.readFromUri(mockUri, mockContext)).thenReturn(mockPhoto);

        Map<String, RequestBody> requestBodyMap = editedMember.formatPatchRequest(mockContext);

        assertFalse(requestBodyMap.containsKey(Member.FIELD_NAME_NATIONAL_ID_PHOTO));
    }

    @Test
    public void formatPatchRequest_clearPhotoDirtyNationalId_IncludesNationalIdPhoto() throws Exception {
        member.setNationalIdPhotoUrl(localNationalIdPhotoUrl);
        Member editedMember = spy(member);
        byte[] mockPhoto = new byte[]{(byte)0xe0};

        when(editedMember.dirty(Member.FIELD_NAME_NATIONAL_ID_PHOTO)).thenReturn(true);
        when(FileManager.isLocal(localNationalIdPhotoUrl)).thenReturn(true);
        when(Uri.parse(localNationalIdPhotoUrl)).thenReturn(mockUri);
        when(FileManager.readFromUri(mockUri, mockContext)).thenReturn(mockPhoto);

        Map<String, RequestBody> requestBodyMap = editedMember.formatPatchRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_NATIONAL_ID_PHOTO).writeTo(buffer);
        assertTrue(Arrays.equals(buffer.readByteArray(), mockPhoto));
    }

    @Test
    public void formatPatchRequest_includesFingerprintsGuid() throws Exception {
        UUID fingerprintsGuid = UUID.randomUUID();
        member.setFingerprintsGuid(fingerprintsGuid);
        Member editedMember = spy(member);

        when(editedMember.dirty(Member.FIELD_NAME_FINGERPRINTS_GUID)).thenReturn(true);

        Map<String, RequestBody> requestBodyMap = editedMember.formatPatchRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_FINGERPRINTS_GUID).writeTo(buffer);
        assertEquals(buffer.readUtf8(), fingerprintsGuid.toString());
    }

    @Test
    public void formatPatchRequest_includesPhoneNumber() throws Exception {
        String phoneNumber = "0765123987";
        member.setPhoneNumber(phoneNumber);
        Member editedMember = spy(member);

        when(editedMember.dirty(Member.FIELD_NAME_PHONE_NUMBER)).thenReturn(true);

        Map<String, RequestBody> requestBodyMap = editedMember.formatPatchRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_PHONE_NUMBER).writeTo(buffer);
        assertEquals(buffer.readUtf8(), phoneNumber);
    }

    @Test
    public void formatPatchRequest_includesFullName() throws Exception {
        String fullName = "Akiiki Monday";
        member.setFullName(fullName);
        Member editedMember = spy(member);

        when(editedMember.dirty(Member.FIELD_NAME_FULL_NAME)).thenReturn(true);

        Map<String, RequestBody> requestBodyMap = editedMember.formatPatchRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_FULL_NAME).writeTo(buffer);
        assertEquals(buffer.readUtf8(), fullName);
    }

    @Test
    public void formatPatchRequest_includesCardId() throws Exception {
        String cardId = "RWI123456";
        member.setCardId(cardId);
        Member editedMember = spy(member);

        when(editedMember.dirty(Member.FIELD_NAME_CARD_ID)).thenReturn(true);

        Map<String, RequestBody> requestBodyMap = editedMember.formatPatchRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_CARD_ID).writeTo(buffer);
        assertEquals(buffer.readUtf8(), cardId);
    }

    @Test
    public void createNewborn() throws Exception {
        UUID householdId = UUID.randomUUID();
        member.setHouseholdId(householdId);

        Member newborn = member.createNewborn();

        assertTrue(newborn.isNew());
        assertEquals(newborn.getHouseholdId(), householdId);
        assertFalse(newborn.getAbsentee());
        assertEquals(newborn.getBirthdateAccuracy(), Member.BirthdateAccuracyEnum.D);
        assertNotNull(newborn.getEnrolledAt());
    }
}
