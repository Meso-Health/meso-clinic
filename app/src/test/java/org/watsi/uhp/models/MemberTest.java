package org.watsi.uhp.models;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

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
import org.watsi.uhp.services.SyncService;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Response;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EncounterDao.class, Bitmap.class, BitmapFactory.class, FileManager.class,
        Member.class, Uri.class, MediaStore.Images.Media.class, File.class, Response.class,
        ApiService.class, ExceptionManager.class, MemberDao.class})
public class MemberTest {
    private final String localPhotoUrl = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
    private final String remotePhotoUrl = "https://d2bxcwowl6jlve.cloudfront.net/media/foo-3bf77f20d8119074";
    private final String localNationalPhotoIdUrl = "https://d2bxcwowl6jlve.cloudfront.net/media/bar-3bf77f20d8119074";
    private final String remoteNationalPhotoIdUrl = "content://org.watsi.uhp.fileprovider/captured_image/national_id.jpg";

    private Member member;

    @Mock
    Context mockContext;
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
    SyncService syncService;

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(ApiService.class);
        mockStatic(ExceptionManager.class);
        mockStatic(MemberDao.class);
        member = new Member();
    }

    @Test
    public void setPhoneNumber() throws Exception {
        member.setPhoneNumber(null);
        assertEquals(member.getPhoneNumber(), null);

        mockStatic(Member.class);
        when(Member.validPhoneNumber(anyString())).thenReturn(true);

        member.setPhoneNumber("0777555555");
        assertEquals(member.getPhoneNumber(), "0777555555");

        when(Member.validPhoneNumber(anyString())).thenReturn(false);
        try {
            member.setPhoneNumber("");
            fail("Should throw validation exception");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "phone_number: Invalid phone number");
        }
    }

    @Test
    public void getPhotoBitmap_photoIsNullandPhotoUrlIsNull() throws Exception {
        ContentResolver mockContentResolver = mock(ContentResolver.class);
        mockStatic(Uri.class);

        assertNull(member.getPhotoBitmap(mockContentResolver));
    }

    @Test
    public void getPhotoBitmap_photoIsNullandPhotoUrlIsNotLocalUrl() throws Exception {
        member.setPhotoUrl(remotePhotoUrl);
        ContentResolver mockContentResolver = mock(ContentResolver.class);
        mockStatic(FileManager.class);

        when(FileManager.isLocal(member.getPhotoUrl())).thenReturn(false);

        assertNull(member.getPhotoBitmap(mockContentResolver));
    }

    @Test
    public void getPhotoBitmap_photoIsNullButLocalPhotoUrl() throws Exception {
        member.setPhotoUrl(localPhotoUrl);
        Uri mockUri = mock(Uri.class);
        Bitmap mockBitmap = mock(Bitmap.class);
        ContentResolver mockContentResolver = mock(ContentResolver.class);
        mockStatic(MediaStore.Images.Media.class);
        mockStatic(Uri.class);
        mockStatic(FileManager.class);

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
        mockStatic(Bitmap.class);
        mockStatic(BitmapFactory.class);
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


    @Test
    public void updatePhotosFromSuccessfulSyncResponse_onlyLocalPhoto_succeeds() throws Exception {
        member.setPhotoUrl(localPhotoUrl);
        member.setId(UUID.randomUUID());
        Member memberSpy = spy(member);

        mockStatic(FileManager.class);
        PowerMockito.stub(PowerMockito.method(FileManager.class, "isLocal")).toReturn(true);

        Member mockResponseMember = mock(Member.class);
        when(mockSyncResponse.body()).thenReturn(mockResponseMember);
        when(mockResponseMember.getPhotoUrl()).thenReturn(remotePhotoUrl);
        doNothing().when(memberSpy).fetchAndSetPhotoFromUrl();

        memberSpy.updatePhotosFromSuccessfulSyncResponse(mockSyncResponse);

        assertEquals(memberSpy.getPhotoUrl(), remotePhotoUrl);
        verify(memberSpy, times(1)).fetchAndSetPhotoFromUrl();
        verifyStatic(times(1));
        FileManager.deleteLocalPhoto(localPhotoUrl);
    }

    @Test
    public void updatePhotosFromSuccessfulSyncResponse_localAndNationalPhoto_succeeds() throws Exception {
        member.setPhotoUrl(localPhotoUrl);
        member.setNationalIdPhotoUrl(localNationalPhotoIdUrl);
        member.setId(UUID.randomUUID());
        Member memberSpy = spy(member);

        mockStatic(FileManager.class);
        PowerMockito.stub(PowerMockito.method(FileManager.class, "isLocal")).toReturn(true);

        Member mockResponseMember = mock(Member.class);
        when(mockSyncResponse.body()).thenReturn(mockResponseMember);
        when(mockResponseMember.getPhotoUrl()).thenReturn(remotePhotoUrl);
        when(mockResponseMember.getNationalIdPhotoUrl()).thenReturn(remoteNationalPhotoIdUrl);
        doNothing().when(memberSpy).fetchAndSetPhotoFromUrl();

        memberSpy.updatePhotosFromSuccessfulSyncResponse(mockSyncResponse);

        assertEquals(memberSpy.getPhotoUrl(), remotePhotoUrl);
        assertEquals(memberSpy.getNationalIdPhotoUrl(), remoteNationalPhotoIdUrl);
        verify(memberSpy, times(1)).fetchAndSetPhotoFromUrl();
        verifyStatic();
        FileManager.deleteLocalPhoto(localPhotoUrl);
        FileManager.deleteLocalPhoto(localNationalPhotoIdUrl);
    }

    @Test
    public void updatePhotosFromSuccessfulSyncResponse_onlyNationalPhoto_succeeds() throws Exception {
        member.setNationalIdPhotoUrl(localNationalPhotoIdUrl);
        member.setId(UUID.randomUUID());
        Member memberSpy = spy(member);

        mockStatic(FileManager.class);
        PowerMockito.stub(PowerMockito.method(FileManager.class, "isLocal")).toReturn(true);

        Member mockResponseMember = mock(Member.class);
        when(mockSyncResponse.body()).thenReturn(mockResponseMember);
        when(mockResponseMember.getNationalIdPhotoUrl()).thenReturn(remoteNationalPhotoIdUrl);
        doNothing().when(memberSpy).fetchAndSetPhotoFromUrl();

        memberSpy.updatePhotosFromSuccessfulSyncResponse(mockSyncResponse);

        assertEquals(memberSpy.getNationalIdPhotoUrl(), remoteNationalPhotoIdUrl);
        verify(memberSpy, never()).fetchAndSetPhotoFromUrl();
        verifyStatic();
        FileManager.deleteLocalPhoto(localNationalPhotoIdUrl);
        verifyStatic(never());
        FileManager.deleteLocalPhoto(localPhotoUrl);
    }

    @Test
    public void formatPatchRequest_newMember() throws Exception {
        member.setIsNew(true);
        try {
            member.formatPatchRequest(mockContext);
            fail("Should throw validation exception");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "is_new: Cannot perform PATCH with new member");
        }
    }

    @Test
    public void formatPatchRequest_dirtyMemberAndNationalIdPhoto_onlyIncludesOnePhoto() throws Exception {
        String uriString = localPhotoUrl;
        byte[] mockPhotoBytes = new byte[]{};
        Context mockContext = mock(Context.class);
        Uri mockUri = mock(Uri.class);
        member.setPhotoUrl(uriString);
        member.setNationalIdPhotoUrl(uriString);
        member.addDirtyField(Member.FIELD_NAME_PHOTO);
        member.addDirtyField(Member.FIELD_NAME_NATIONAL_ID_PHOTO);
        member.setIsNew(false);

        mockStatic(Uri.class);
        mockStatic(FileManager.class);
        when(Uri.parse(uriString)).thenReturn(mockUri);
        when(FileManager.readFromUri(mockUri, mockContext)).thenReturn(mockPhotoBytes);

        Map<String, RequestBody> firstRequestBody = member.formatPatchRequest(mockContext);

        assertFalse(member.dirty(Member.FIELD_NAME_PHOTO));
        assertTrue(member.dirty(Member.FIELD_NAME_NATIONAL_ID_PHOTO));
        assertNotNull(firstRequestBody.get(Member.FIELD_NAME_PHOTO));
        assertNull(firstRequestBody.get(Member.FIELD_NAME_NATIONAL_ID_PHOTO));

        Map<String, RequestBody> secondRequestBody = member.formatPatchRequest(mockContext);

        assertFalse(member.dirty(Member.FIELD_NAME_NATIONAL_ID_PHOTO));
        assertNotNull(secondRequestBody.get(Member.FIELD_NAME_NATIONAL_ID_PHOTO));
        assertNull(secondRequestBody.get(Member.FIELD_NAME_PHOTO));
        assertFalse(member.isDirty());
    }

    @Test
    public void formatPostRequest_existingMember() throws Exception {
        member.setIsNew(false);
        try {
            member.formatPostRequest(mockContext);
            fail("Should throw validation exception");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "is_new: Cannot perform POST with existing member");
        }
    }

    @Test
    public void formatPostRequest_nullId() throws Exception {
        member.setIsNew(true);
        member.setId(null);
        try {
            member.formatPostRequest(mockContext);
            fail("Should throw validation exception");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "id: Cannot be null");
        }
    }

    @Test
    public void formatPostRequest_newMember() throws Exception {
        String fullName = "Akiiki Monday";
        String cardId = "RWI111111";
        String photoUrl = localPhotoUrl;
        mockStatic(FileManager.class);
        mockStatic(Uri.class);
        Uri mockUri = mock(Uri.class);
        byte[] mockPhoto = new byte[]{};
        Member memberSpy = spy(Member.class);
        memberSpy.setBirthdate(Calendar.getInstance().getTime());
        memberSpy.setBirthdateAccuracy(Member.BirthdateAccuracyEnum.D);
        memberSpy.setId(UUID.randomUUID());
        memberSpy.setGender(Member.GenderEnum.F);
        memberSpy.setFullName(fullName);
        memberSpy.setCardId(cardId);
        memberSpy.setPhotoUrl(photoUrl);
        memberSpy.setIsNew(true);
        memberSpy.setHouseholdId(UUID.randomUUID());
        memberSpy.setEnrolledAt(Calendar.getInstance().getTime());

        when(Uri.parse(memberSpy.getPhotoUrl())).thenReturn(mockUri);
        when(FileManager.readFromUri(mockUri, mockContext)).thenReturn(mockPhoto);
        when(FileManager.isLocal(memberSpy.getPhotoUrl())).thenReturn(true);

        Map<String, RequestBody> requestBodyMap = memberSpy.formatPostRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_ID).writeTo(buffer);
        assertEquals(buffer.readUtf8(), memberSpy.getId().toString());
        buffer.clear();

        requestBodyMap.get(Member.FIELD_NAME_GENDER).writeTo(buffer);
        assertEquals(buffer.readUtf8(), "F");
        buffer.clear();

        requestBodyMap.get(Member.FIELD_NAME_FULL_NAME).writeTo(buffer);
        assertEquals(buffer.readUtf8(), fullName);
        buffer.clear();

        requestBodyMap.get(Member.FIELD_NAME_CARD_ID).writeTo(buffer);
        assertEquals(buffer.readUtf8(), cardId);
        buffer.clear();

        requestBodyMap.get("provider_assignment[provider_id]").writeTo(buffer);
        assertEquals(buffer.readUtf8(), "1");
        buffer.clear();

        requestBodyMap.get("provider_assignment[start_reason]").writeTo(buffer);
        assertEquals(buffer.readUtf8(), "birth");
        buffer.clear();

        requestBodyMap.get(Member.FIELD_NAME_BIRTHDATE_ACCURACY).writeTo(buffer);
        assertEquals(buffer.readUtf8(), "D");
        buffer.clear();

        requestBodyMap.get(Member.FIELD_NAME_BIRTHDATE).writeTo(buffer);
        assertEquals(buffer.readUtf8(), Clock.asIso(memberSpy.getBirthdate()));
        buffer.clear();

        requestBodyMap.get(Member.FIELD_NAME_ENROLLED_AT).writeTo(buffer);
        assertEquals(buffer.readUtf8(), Clock.asIso(memberSpy.getEnrolledAt()));
        buffer.clear();

        verify(memberSpy, times(1)).clearDirtyFields();
    }

    @Test
    public void createNewborn() throws Exception {
        UUID householdId = UUID.randomUUID();
        member.setHouseholdId(householdId);

        Member newborn = member.createNewborn();

        assertTrue(newborn.isNew());
        assertNotNull(newborn.getId());
        assertEquals(newborn.getHouseholdId(), householdId);
        assertFalse(newborn.getAbsentee());
        assertEquals(newborn.getBirthdateAccuracy(), Member.BirthdateAccuracyEnum.D);
        assertNotNull(newborn.getEnrolledAt());
    }

    @Test
    public void syncMember_dirtyMember_succeeds() throws Exception {
        member.setId(UUID.randomUUID());
        Member memberSpy = spy(member);

        doReturn(mockMemberCall).when(memberSpy).createSyncMemberRequest(syncService);
        doReturn(mockMemberSyncResponse).when(mockMemberCall).execute();
        doNothing().when(memberSpy).updatePhotosFromSuccessfulSyncResponse(any(Response.class));
        doReturn(true).when(memberSpy).isDirty();
        doReturn(true).when(mockMemberSyncResponse).isSuccessful();
        doReturn(true).when(memberSpy).isNew();

        memberSpy.syncMember(syncService);

        verify(memberSpy, times(1)).updatePhotosFromSuccessfulSyncResponse(any(Response.class));
        verify(memberSpy, never()).setSynced();

        verifyStatic();
        MemberDao.update(memberSpy);
        ExceptionManager.requestFailure(
                anyString(), any(Request.class),
                any(okhttp3.Response.class), anyMapOf(String.class, String.class));
    }

    @Test
    public void syncMember_nonDirtyMember_succeeds() throws Exception {
        member.setId(UUID.randomUUID());
        Member memberSpy = spy(member);

        doReturn(mockMemberCall).when(memberSpy).createSyncMemberRequest(syncService);
        doReturn(mockMemberSyncResponse).when(mockMemberCall).execute();
        doNothing().when(memberSpy).updatePhotosFromSuccessfulSyncResponse(any(Response.class));
        doReturn(false).when(memberSpy).isDirty();
        doReturn(true).when(mockMemberSyncResponse).isSuccessful();
        doReturn(true).when(memberSpy).isNew();

        memberSpy.syncMember(syncService);

        verify(memberSpy, times(1)).updatePhotosFromSuccessfulSyncResponse(any(Response.class));
        verify(memberSpy, times(1)).setSynced();

        verifyStatic();
        MemberDao.update(memberSpy);
        ExceptionManager.requestFailure(
                anyString(), any(Request.class),
                any(okhttp3.Response.class), anyMapOf(String.class, String.class));
    }

    @Test
    public void syncMember_unSuccessfulResponse_fails() throws Exception {
        member.setId(UUID.randomUUID());
        Member memberSpy = spy(member);

        doReturn(mockMemberCall).when(memberSpy).createSyncMemberRequest(syncService);
        doReturn(mockMemberSyncResponse).when(mockMemberCall).execute();
        doReturn(false).when(mockMemberSyncResponse).isSuccessful();
        doReturn(true).when(memberSpy).isNew();

        memberSpy.syncMember(syncService);

        verify(memberSpy, never()).updatePhotosFromSuccessfulSyncResponse(any(Response.class));
        verify(memberSpy, never()).setSynced();

        verifyStatic(never());
        MemberDao.update(memberSpy);

        verifyStatic();
        ExceptionManager.requestFailure(
                anyString(), any(Request.class),
                any(okhttp3.Response.class), anyMapOf(String.class, String.class));
    }

    @Test
    public void createSyncMemberRequest_newMember_succeeds() throws Exception {
        member.setId(UUID.randomUUID());

        Member memberSpy = spy(member);

        doReturn(true).when(memberSpy).isNew();
        when(ApiService.requestBuilder(syncService)).thenReturn(mockApi);
        doReturn(mockRequestBodyMap).when(memberSpy).formatPostRequest(syncService);
        when(mockApi.enrollMember(
                memberSpy.getTokenAuthHeaderString(), mockRequestBodyMap))
                .thenReturn(mockMemberCall);
        when(mockMemberSyncResponse.isSuccessful()).thenReturn(true);

        memberSpy.createSyncMemberRequest(syncService);

        verifyStatic(never());
        ExceptionManager.reportException(any(Exception.class));
        ExceptionManager.requestFailure(
                anyString(), any(Request.class),
                any(okhttp3.Response.class), anyMapOf(String.class, String.class));
        verify(mockApi, times(1)).enrollMember(memberSpy.getTokenAuthHeaderString(), mockRequestBodyMap);
        verify(memberSpy, times(1)).formatPostRequest(syncService);
    }

    @Test
    public void createSyncMemberRequest_existingMember_succeeds() throws Exception {
        member.setId(UUID.randomUUID());
        Member memberSpy = spy(member);

        doReturn(false).when(memberSpy).isNew();
        when(ApiService.requestBuilder(syncService)).thenReturn(mockApi);
        doReturn(mockRequestBodyMap).when(memberSpy).formatPatchRequest(syncService);
        when(mockApi.syncMember(
                memberSpy.getTokenAuthHeaderString(), memberSpy.getId(), mockRequestBodyMap))
                .thenReturn(mockMemberCall);
        when(mockMemberSyncResponse.isSuccessful()).thenReturn(true);

        memberSpy.createSyncMemberRequest(syncService);

        verifyStatic(never());
        ExceptionManager.reportException(any(Exception.class));
        ExceptionManager.requestFailure(
                anyString(), any(Request.class),
                any(okhttp3.Response.class), anyMapOf(String.class, String.class));
        verify(mockApi, times(1)).syncMember(memberSpy.getTokenAuthHeaderString(), memberSpy.getId(), mockRequestBodyMap);
        verify(memberSpy, times(1)).formatPatchRequest(syncService);
    }
}
