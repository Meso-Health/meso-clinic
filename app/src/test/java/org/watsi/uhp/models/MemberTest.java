package org.watsi.uhp.models;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.URLUtil;

import com.google.common.io.ByteStreams;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.managers.ExceptionManager;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AbstractModel.class, ApiService.class, Bitmap.class, ByteStreams.class, File.class,
        ExceptionManager.class, MediaStore.Images.Media.class, Member.class,
        okhttp3.Response.class, Request.class, Response.class, ResponseBody.class, Uri.class,
        URLUtil.class})
public class MemberTest {
    private final String localPhotoUrl = "content://org.watsi.uhp.fileprovider/captured_image/photo.jpg";
    private final String remotePhotoUrl = "https://d2bxcwowl6jlve.cloudfront.net/media/foo-3bf77f20d8119074";
    private final String localNationalIdPhotoUrl = "content://org.watsi.uhp.fileprovider/captured_image/national_id.jpg";

    private Member member;

    @Mock Photo mockMemberPhoto;

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(AbstractModel.class);
        mockStatic(ApiService.class);
        mockStatic(Bitmap.class);
        mockStatic(ByteStreams.class);
        mockStatic(ExceptionManager.class);
        mockStatic(MediaStore.Images.Media.class);
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

    @Test
    public void createNewborn() throws Exception {
        UUID householdId = UUID.randomUUID();
        member.setHouseholdId(householdId);

        Member newborn = member.createNewborn();

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

    @Test
    public void validNonEmptyCardId() {
        assertTrue(Member.validNonNullCardId("RWI123123"));
        assertTrue(Member.validNonNullCardId("RWI 123 123"));
        assertTrue(Member.validNonNullCardId("RWI123 123"));
        assertFalse(Member.validNonNullCardId("LALALA BANANAPHONE 123"));
        assertFalse(Member.validNonNullCardId(""));
        assertFalse(Member.validNonNullCardId("   "));
        assertFalse(Member.validNonNullCardId(null));
    }
}
