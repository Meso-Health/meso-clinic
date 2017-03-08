package org.watsi.uhp.models;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.managers.FileManager;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EncounterDao.class, Bitmap.class, BitmapFactory.class, FileManager.class,
        Member.class, Uri.class, MediaStore.Images.Media.class})
public class MemberTest {

    private Member member;

    @Before
    public void setup() {
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
    public void equals_returnsTrueIfSameObject() throws Exception {
        assertTrue(member.equals(member));
    }

    @Test
    public void equals_returnsTrueIfSameId() throws Exception {
        UUID id = UUID.randomUUID();
        Member member1 = new Member();
        member1.setId(id);
        Member member2 = new Member();
        member2.setId(id);

        assertTrue(member1.equals(member2));
    }

    @Test
    public void equals_returnsFalseIfDifferentId() throws Exception {
        Member member1 = new Member();
        member1.setId(UUID.randomUUID());
        Member member2 = new Member();
        member2.setId(UUID.randomUUID());

        assertFalse(member1.equals(member2));
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
}
