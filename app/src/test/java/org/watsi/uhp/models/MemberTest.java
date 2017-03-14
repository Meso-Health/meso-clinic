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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.managers.FileManager;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import okhttp3.RequestBody;
import okio.Buffer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EncounterDao.class, Bitmap.class, BitmapFactory.class, FileManager.class,
        Member.class, Uri.class, MediaStore.Images.Media.class, File.class, ConfigManager.class})
public class MemberTest {

    private Member member;

    @Mock
    private Context mockContext;

    @Before
    public void setup() {
        initMocks(this);
        member = new Member();
    }

    @Test
    public void emptyConstructor_setsIsNewAndId() throws Exception {
        Member newMember = new Member();
        assertTrue(newMember.isNew());
        assertNotNull(newMember.getId());
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
        member.setPhotoUrl("https://d2bxcwowl6jlve.cloudfront.net/media/foo-3bf77f20d8119074");
        ContentResolver mockContentResolver = mock(ContentResolver.class);
        mockStatic(FileManager.class);

        when(FileManager.isLocal(member.getPhotoUrl())).thenReturn(false);

        assertNull(member.getPhotoBitmap(mockContentResolver));
    }

    @Test
    public void getPhotoBitmap_photoIsNullButLocalPhotoUrl() throws Exception {
        member.setPhotoUrl("content://org.watsi.uhp.fileprovider/captured_image/photo.jpg");
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
    public void deleteLocalMemberImage_nullPhotoUrl() throws Exception {
        Member memberSpy = spy(Member.class);
        memberSpy.setPhotoUrl(null);
        File mockFile = mock(File.class);

        whenNew(File.class).withAnyArguments().thenReturn(mockFile);

        memberSpy.deleteLocalMemberImage();

        verify(mockFile, never()).delete();
    }

    @Test
    public void deleteLocalMemberImage_remotePhotoUrl() throws Exception {
        Member memberSpy = spy(Member.class);
        memberSpy.setPhotoUrl("https://d2bxcwowl6jlve.cloudfront.net/media/foo-3bf77f20d8119074");
        File mockFile = mock(File.class);
        mockStatic(FileManager.class);

        whenNew(File.class).withAnyArguments().thenReturn(mockFile);
        when(FileManager.isLocal(memberSpy.getPhotoUrl())).thenReturn(false);

        memberSpy.deleteLocalMemberImage();

        verify(mockFile, never()).delete();
        verify(memberSpy, never()).setPhotoUrl(null);
    }

    @Test
    public void deleteLocalMemberImage_localPhotoUrl() throws Exception {
        Member memberSpy = spy(Member.class);
        memberSpy.setPhotoUrl("content://org.watsi.uhp.fileprovider/captured_image/photo.jpg");
        File mockFile = mock(File.class);
        mockStatic(FileManager.class);

        whenNew(File.class).withAnyArguments().thenReturn(mockFile);
        when(FileManager.isLocal(memberSpy.getPhotoUrl())).thenReturn(true);

        memberSpy.deleteLocalMemberImage();

        verify(mockFile).delete();
        verify(memberSpy, times(1)).setPhotoUrl(null);
    }

    @Test
    public void deleteLocalIdImage_nullPhotoUrl() throws Exception {
        Member memberSpy = spy(Member.class);
        memberSpy.setNationalIdPhoto(null);
        File mockFile = mock(File.class);

        whenNew(File.class).withAnyArguments().thenReturn(mockFile);

        memberSpy.deleteLocalIdImage();

        verify(mockFile, never()).delete();
    }

    @Test
    public void deleteLocalIdImage_remotePhotoUrl() throws Exception {
        Member memberSpy = spy(Member.class);
        memberSpy.setNationalIdPhotoUrl("https://d2bxcwowl6jlve.cloudfront.net/media/foo-3bf77f20d8119074");
        File mockFile = mock(File.class);
        mockStatic(FileManager.class);

        whenNew(File.class).withAnyArguments().thenReturn(mockFile);
        when(FileManager.isLocal(memberSpy.getNationalIdPhotoUrl())).thenReturn(false);

        memberSpy.deleteLocalIdImage();

        verify(mockFile, never()).delete();
        verify(memberSpy, never()).setNationalIdPhotoUrl(null);
    }

    @Test
    public void deleteLocalIdImage_localPhotoUrl() throws Exception {
        Member memberSpy = spy(Member.class);
        memberSpy.setNationalIdPhotoUrl("content://org.watsi.uhp.fileprovider/captured_image/photo.jpg");
        File mockFile = mock(File.class);
        mockStatic(FileManager.class);

        whenNew(File.class).withAnyArguments().thenReturn(mockFile);
        when(FileManager.isLocal(memberSpy.getNationalIdPhotoUrl())).thenReturn(true);

        memberSpy.deleteLocalIdImage();

        verify(mockFile).delete();
        verify(memberSpy, times(1)).setNationalIdPhotoUrl(null);
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
        String uriString = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
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
        when(FileManager.readFromUri(mockUri,mockContext)).thenReturn(mockPhotoBytes);

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
    public void formatPostRequest_newMember() throws Exception {
        String fullName = "Akiiki Monday";
        int providerId = 1;
        String cardId = "RWI111111";
        String photoUrl = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
        mockStatic(ConfigManager.class);
        mockStatic(FileManager.class);
        mockStatic(Uri.class);
        Uri mockUri = mock(Uri.class);
        byte[] mockPhoto = new byte[]{};
        Member memberSpy = spy(Member.class);
        memberSpy.setGender(Member.GenderEnum.F);
        memberSpy.setFullName(fullName);
        memberSpy.setCardId(cardId);
        memberSpy.setPhotoUrl(photoUrl);
        memberSpy.setIsNew(true);

        when(Uri.parse(memberSpy.getPhotoUrl())).thenReturn(mockUri);
        when(ConfigManager.getProviderId(mockContext)).thenReturn(providerId);
        when(FileManager.readFromUri(mockUri, mockContext)).thenReturn(mockPhoto);
        when(FileManager.isLocal(memberSpy.getPhotoUrl())).thenReturn(true);

        Map<String,RequestBody> requestBodyMap = memberSpy.formatPostRequest(mockContext);

        Buffer buffer = new Buffer();
        requestBodyMap.get(Member.FIELD_NAME_GENDER).writeTo(buffer);
        assertEquals(buffer.readUtf8(), "F");
        buffer.clear();

        requestBodyMap.get(Member.FIELD_NAME_FULL_NAME).writeTo(buffer);
        assertEquals(buffer.readUtf8(), fullName);
        buffer.clear();

        requestBodyMap.get(Member.FIELD_NAME_CARD_ID).writeTo(buffer);
        assertEquals(buffer.readUtf8(), cardId);
        buffer.clear();

        requestBodyMap.get("provider_assignment").writeTo(buffer);
        assertEquals(buffer.readUtf8(), "{\"provider_id\":1,\"start_reason\":\"birth\"}");
        buffer.clear();

        verify(memberSpy, times(1)).removeDirtyField(Member.FIELD_NAME_PHOTO);
        verify(memberSpy, times(1)).removeDirtyField(Member.FIELD_NAME_GENDER);
        verify(memberSpy, times(1)).removeDirtyField(Member.FIELD_NAME_FULL_NAME);
        verify(memberSpy, times(1)).removeDirtyField(Member.FIELD_NAME_CARD_ID);
    }
}
