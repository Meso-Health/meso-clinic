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

import java.util.Arrays;
import java.util.Calendar;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
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
    public void getLastEncounter() throws Exception {
        Calendar cal = Calendar.getInstance();
        Encounter e1 = new Encounter();
        e1.setDate(cal.getTime());
        Encounter e2 = new Encounter();
        cal.add(Calendar.DAY_OF_MONTH, -5);
        e2.setDate(cal.getTime());

        mockStatic(EncounterDao.class);
        when(EncounterDao.find(Mockito.anyMap())).thenReturn(Arrays.asList(new Encounter[]{e2, e1}));

        assertEquals(member.getLastEncounter(), e1);
    }
}
