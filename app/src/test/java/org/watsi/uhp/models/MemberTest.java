package org.watsi.uhp.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.URLUtil;

import com.google.common.io.ByteStreams;
import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.api.UhpApi;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;

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
@PrepareForTest({ApiService.class, Bitmap.class, ByteStreams.class, EncounterDao.class, File.class,
        ExceptionManager.class, MediaStore.Images.Media.class, Member.class, MemberDao.class,
        okhttp3.Response.class, Request.class, Response.class, ResponseBody.class, Uri.class,
        URLUtil.class})
public class MemberTest {
    private final String localPhotoUrl = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
    private final String remotePhotoUrl = "https://d2bxcwowl6jlve.cloudfront.net/media/foo-3bf77f20d8119074";
    private final String localNationalIdPhotoUrl = "content://org.watsi.uhp.fileprovider/captured_image/national_id.jpg";

    private Member member;

    @Mock
    Response<Member> mockSyncResponse;
    @Mock
    Photo mockMemberPhoto;
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
        mockStatic(ByteStreams.class);
        mockStatic(ExceptionManager.class);
        mockStatic(MediaStore.Images.Media.class);
        mockStatic(MemberDao.class);
        mockStatic(Uri.class);
        mockStatic(URLUtil.class);
        when(URLUtil.isValidUrl(remotePhotoUrl)).thenReturn(true);
        when(URLUtil.isValidUrl(localPhotoUrl)).thenReturn(true);
        when(URLUtil.isValidUrl(localNationalIdPhotoUrl)).thenReturn(true);
        member = new Member();
    }

    @Test
    public void setPhoneNumber_isNull_setsPhoneNumberToNull() throws Exception {
        member.setPhoneNumber(null);

        assertEquals(member.getPhoneNumber(), null);
    }

    @Test
    public void setPhoneNumber_isNull_setsPhoneNumberToEmptyString() throws Exception {
        member.setPhoneNumber("");

        assertEquals(member.getPhoneNumber(), null);
    }

    @Test
    public void setPhoneNumber_isValid_setsPhoneNumber() throws Exception {
        mockStatic(Member.class);
        when(Member.validPhoneNumber(anyString())).thenReturn(true);

        member.setPhoneNumber("0777555555");

        assertEquals(member.getPhoneNumber(), "0777555555");
    }

    @Test
    public void isAbsentee_isUnder6_hasPhoto_hasNoFingerprints_returnsFalse() throws Exception {
        member.setAge(5);
        member.setLocalMemberPhoto(mockMemberPhoto);
        member.setFingerprintsGuid(null);

        assertEquals(member.isAbsentee(), false);
    }

    @Test
    public void isAbsentee_isUnder6_hasNoPhoto_hasNoFingerprints_returnsTrue() throws Exception {
        member.setAge(5);
        member.setLocalMemberPhoto(null);
        member.setFingerprintsGuid(null);

        assertEquals(member.isAbsentee(), true);
    }

    @Test
    public void isAbsentee_isOver6_hasPhoto_hasFingerprints_returnsFalse() throws Exception {
        member.setAge(7);
        member.setLocalMemberPhoto(mockMemberPhoto);
        member.setFingerprintsGuid(UUID.randomUUID());

        assertEquals(member.isAbsentee(), false);
    }

    @Test
    public void isAbsentee_isOver6_hasNoPhoto_hasFingerprints_returnsTrue() throws Exception {
        member.setAge(7);
        member.setLocalMemberPhoto(null);
        member.setFingerprintsGuid(UUID.randomUUID());

        assertEquals(member.isAbsentee(), true);
    }

    @Test
    public void isAbsentee_isOver6_hasPhoto_hasNoFingerprints_returnsTrue() throws Exception {
        member.setAge(7);
        member.setLocalMemberPhoto(mockMemberPhoto);
        member.setFingerprintsGuid(null);

        assertEquals(member.isAbsentee(), true);
    }

    @Test
    public void isAbsentee_isOver6_hasNoPhoto_hasNoFingerprints_returnsTrue() throws Exception {
        member.setAge(7);
        member.setLocalMemberPhoto(null);
        member.setFingerprintsGuid(null);

        assertEquals(member.isAbsentee(), true);
    }

    private void mockPhotoFetch() throws Exception {
        doNothing().when(mockMemberPhoto).markAsSynced();
        whenNew(Request.Builder.class).withNoArguments().thenReturn(mockRequestBuilder);
        when(mockRequestBuilder.url(BuildConfig.API_HOST + remotePhotoUrl)).thenReturn(mockRequestBuilder);
        when(mockRequestBuilder.build()).thenReturn(mockRequest);
        whenNew(OkHttpClient.class).withNoArguments().thenReturn(mockHttpClient);
        when(mockHttpClient.newCall(mockRequest)).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
    }

    @Test
    public void handleUpdateFromSync_responseHasPhotoUrl_setsAndFetchesPhoto() throws Exception {
        String previousPhotoUrl = "prevUrl";
        when(URLUtil.isValidUrl(previousPhotoUrl)).thenReturn(true);
        member.setRemoteMemberPhotoUrl(previousPhotoUrl);
        Member memberSpy = spy(member);
        memberSpy.setLocalMemberPhoto(mockMemberPhoto);
        Member responseMember = new Member();
        responseMember.setRemoteMemberPhotoUrl(remotePhotoUrl);

        mockPhotoFetch();
        doNothing().when(memberSpy).fetchAndSetPhotoFromUrl(mockHttpClient);
        when(mockMemberSyncResponse.body()).thenReturn(responseMember);

        memberSpy.handleUpdateFromSync(responseMember);

        verify(mockMemberPhoto).markAsSynced();
        assertEquals(memberSpy.getRemoteMemberPhotoUrl(), remotePhotoUrl);
        verify(memberSpy).fetchAndSetPhotoFromUrl(mockHttpClient);
        assertEquals(responseMember.getLocalMemberPhoto(), mockMemberPhoto);
    }

    @Test
    public void handleUpdateFromSync_responseHasNationalIdPhotoUrl_setsExisting() throws Exception {
        String previousPhotoUrl = "prevUrl";
        member.setRemoteNationalIdPhotoUrl(previousPhotoUrl);
        Member memberSpy = spy(member);
        Photo mockNationalIdPhoto = mock(Photo.class);
        memberSpy.setLocalNationalIdPhoto(mockNationalIdPhoto);
        Member responseMember = new Member();
        responseMember.setRemoteNationalIdPhotoUrl(remotePhotoUrl);

        when(mockMemberSyncResponse.body()).thenReturn(responseMember);

        memberSpy.handleUpdateFromSync(responseMember);

        verify(mockNationalIdPhoto).markAsSynced();
        assertEquals(responseMember.getLocalNationalIdPhoto(), mockNationalIdPhoto);
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
    }

    @Test
    public void updateFromFetch_persistedMemberSyncedSamePhoto_setsPhotoAndUpdates() throws Exception {
        member.setId(UUID.randomUUID());
        member.setRemoteMemberPhotoUrl(remotePhotoUrl);
        Member memberSpy = spy(member);
        byte[] photoBytes = new byte[]{(byte)0xe0};
        Member persistedMember = new Member();
        persistedMember.setLocalMemberPhoto(mockMemberPhoto);
        persistedMember.setRemoteMemberPhotoUrl(remotePhotoUrl);
        persistedMember.setCroppedPhotoBytes(photoBytes);
        Member persistedMemberSpy = spy(persistedMember);

        when(persistedMemberSpy.isSynced()).thenReturn(true);
        when(MemberDao.findById(memberSpy.getId())).thenReturn(persistedMemberSpy);
        doReturn(mockDao).when(memberSpy).getDao();

        memberSpy.updateFromFetch();

        verify(mockDao, times(1)).createOrUpdate(memberSpy);
        verify(memberSpy, times(1)).setCroppedPhotoBytes(photoBytes);
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
        member.setRemoteMemberPhotoUrl(null);

        whenNew(Request.Builder.class).withNoArguments().thenReturn(mockRequestBuilder);
        when(mockRequestBuilder.url(localPhotoUrl)).thenReturn(mockRequestBuilder);
        mockPhotoFetch();

        member.fetchAndSetPhotoFromUrl(mockHttpClient);

        verify(mockRequestBuilder, never()).build();
    }

    @Test
    public void fetchAndSetPhotoFromUrl_remoteUrlFetchSucceeds_setsPhoto() throws Exception {
        member.setRemoteMemberPhotoUrl(remotePhotoUrl);
        byte[] photoBytes = new byte[]{(byte)0xe0};
        // mock with PowerMockito because we want to mock byteStream which is a final method
        ResponseBody mockResponseBody = PowerMockito.mock(ResponseBody.class);

        mockPhotoFetch();
        when(mockResponse.isSuccessful()).thenReturn(true);
        doReturn(mockResponseBody).when(mockResponse).body();
        when(mockResponse.body()).thenReturn(mockResponseBody);
        PowerMockito.when(mockResponseBody.byteStream()).thenReturn(mockInputStream);
        when(ByteStreams.toByteArray(mockInputStream)).thenReturn(photoBytes);

        member.fetchAndSetPhotoFromUrl(mockHttpClient);

        assertEquals(member.getCroppedPhotoBytes(), photoBytes);
        verify(mockResponse, times(1)).close();
    }

    @Test
    public void fetchAndSetPhotoFromUrl_remoteUrlFetchFails_reportsFailure() throws Exception {
        member.setId(UUID.randomUUID());
        member.setRemoteMemberPhotoUrl(remotePhotoUrl);
        member.setCroppedPhotoBytes(null);

        mockPhotoFetch();
        when(mockResponse.isSuccessful()).thenReturn(false);

        member.fetchAndSetPhotoFromUrl(mockHttpClient);

        assertNull(member.getCroppedPhotoBytes());
        verifyStatic();
        ExceptionManager.requestFailure(
                anyString(), any(Request.class), any(okhttp3.Response.class),
                anyMapOf(String.class, String.class));
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
    public void validPhoneNumberStatic() throws Exception {
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
        newborn.setLocalMemberPhoto(null);

        Map<String, RequestBody> requestBodyMap = newborn.formatPostRequest(mockContext);

        assertFalse(requestBodyMap.containsKey(Member.API_NAME_MEMBER_PHOTO));
    }


    @Test
    public void formatPostRequest_validMemberHasPhotoUrl_includesPhoto() throws Exception {
        Member newborn = spy(mockNewborn());
        newborn.setLocalMemberPhoto(mockMemberPhoto);
        byte[] mockPhotoBytes = new byte[]{(byte)0xe0};

        when(mockMemberPhoto.bytes(mockContext)).thenReturn(mockPhotoBytes);
        when(Uri.parse(localPhotoUrl)).thenReturn(mockUri);

        Map<String, RequestBody> requestBodyMap = newborn.formatPostRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.API_NAME_MEMBER_PHOTO).writeTo(buffer);
        assertTrue(Arrays.equals(buffer.readByteArray(), mockMemberPhoto.bytes(mockContext)));
    }

    @Test
    public void formatPatchRequest_dirtyPhoto_includesPhoto() throws Exception {
        Member editedMember = spy(member);
        editedMember.setLocalMemberPhoto(mockMemberPhoto);
        byte[] mockPhotoBytes = new byte[]{(byte)0xe0};

        when(editedMember.dirty(Member.FIELD_NAME_LOCAL_MEMBER_PHOTO_ID)).thenReturn(true);
        when(mockMemberPhoto.bytes(mockContext)).thenReturn(mockPhotoBytes);
        when(Uri.parse(localPhotoUrl)).thenReturn(mockUri);

        Map<String, RequestBody> requestBodyMap = editedMember.formatPatchRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.API_NAME_MEMBER_PHOTO).writeTo(buffer);
        assertTrue(Arrays.equals(buffer.readByteArray(), mockPhotoBytes));
    }

    @Test
    public void formatPatchRequest_dirtyPhotoAndNationalId_doesNotIncludeNationalIdPhoto() throws Exception {
        member.setLocalMemberPhoto(mockMemberPhoto);
        member.setLocalNationalIdPhoto(mockMemberPhoto);
        Member editedMember = spy(member);
        byte[] mockPhotoBytes = new byte[]{(byte)0xe0};

        when(mockMemberPhoto.bytes(mockContext)).thenReturn(mockPhotoBytes);
        when(editedMember.dirty(Member.API_NAME_MEMBER_PHOTO)).thenReturn(true);
        when(editedMember.dirty(Member.API_NAME_NATIONAL_ID_PHOTO)).thenReturn(true);
        when(Uri.parse(localPhotoUrl)).thenReturn(mockUri);
        when(Uri.parse(localNationalIdPhotoUrl)).thenReturn(mockUri);

        Map<String, RequestBody> requestBodyMap = editedMember.formatPatchRequest(mockContext);

        assertFalse(requestBodyMap.containsKey(Member.API_NAME_NATIONAL_ID_PHOTO));
    }

    @Test
    public void formatPatchRequest_cleanPhotoDirtyNationalId_IncludesNationalIdPhoto() throws Exception {
        member.setLocalNationalIdPhoto(mockMemberPhoto);
        Member editedMember = spy(member);
        byte[] mockPhotoBytes = new byte[]{(byte)0xe0};

        when(mockMemberPhoto.bytes(mockContext)).thenReturn(mockPhotoBytes);
        when(editedMember.dirty(Member.API_NAME_MEMBER_PHOTO)).thenReturn(false);
        when(editedMember.dirty(Member.API_NAME_NATIONAL_ID_PHOTO)).thenReturn(true);
        when(Uri.parse(localNationalIdPhotoUrl)).thenReturn(mockUri);

        Map<String, RequestBody> requestBodyMap = editedMember.formatPatchRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.API_NAME_NATIONAL_ID_PHOTO).writeTo(buffer);
        assertTrue(Arrays.equals(buffer.readByteArray(), mockPhotoBytes));
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
        assertEquals(newborn.getBirthdateAccuracy(), Member.BirthdateAccuracyEnum.D);
        assertNotNull(newborn.getEnrolledAt());
    }

    @Test
    public void validFullName() {
        member.setFullName("Valid Full Name");
        assertTrue(member.validFullName());
        member.setFullName("");
        assertFalse(member.validFullName());
        member.setFullName(null);
        assertFalse(member.validFullName());
    }

    @Test
    public void validPhoneNumber() {
        member.setPhoneNumber("123123123");
        assertTrue(member.validPhoneNumber());
        member.setPhoneNumber("123123");
        assertFalse(member.validPhoneNumber());
        member.setPhoneNumber("");
        assertTrue(member.validPhoneNumber());
        member.setPhoneNumber(null);
        assertTrue(member.validPhoneNumber());
    }

    @Test
    public void validBirthdate() {
        Calendar cal = Calendar.getInstance();

        member.setBirthdateAccuracy(Member.BirthdateAccuracyEnum.D);
        member.setBirthdate(cal.getTime());
        assertTrue(member.validBirthdate());

        member.setBirthdateAccuracy(Member.BirthdateAccuracyEnum.D);
        member.setBirthdate(null);
        assertFalse(member.validBirthdate());

        member.setBirthdateAccuracy(null);
        member.setBirthdate(cal.getTime());
        assertFalse(member.validBirthdate());

        member.setBirthdateAccuracy(null);
        member.setBirthdate(null);
        assertFalse(member.validBirthdate());
    }

    @Test
    public void validGender() {
        member.setGender(null);
        assertFalse(member.validGender());
        member.setGender(Member.GenderEnum.F);
        assertTrue(member.validGender());
        member.setGender(Member.GenderEnum.M);
        assertTrue(member.validGender());
    }
}
