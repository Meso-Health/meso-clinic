package org.watsi.uhp.models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.database.EncounterDao;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EncounterDao.class, Bitmap.class, BitmapFactory.class})
public class MemberTest {

    private Member member;

    @Before
    public void setup() {
        member = new Member();
    }

    @Test
    public void getPhotoBitmap_photoIsNull() throws Exception {
        assertNull(member.getPhotoBitmap());
    }

    @Test
    public void getPhotoBitmap_photoIsNotNull() throws Exception {
        byte[] photoBytes = new byte[]{};
        member.setPhoto(photoBytes);
        mockStatic(Bitmap.class);
        mockStatic(BitmapFactory.class);
        Bitmap bitmap = Mockito.mock(Bitmap.class);

        when(Bitmap.createBitmap(any(Bitmap.class))).thenReturn(bitmap);
        when(BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length)).thenReturn(bitmap);

        assertEquals(member.getPhotoBitmap(), bitmap);
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
}
