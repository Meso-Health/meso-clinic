package org.watsi.uhp.models;

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
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EncounterDao.class)
public class MemberTest {

    private Member member;

    @Before
    public void setup() {
        mockStatic(EncounterDao.class);
        member = new Member();
    }

    @Test
    public void getLastEncounter() throws Exception {
        Calendar cal = Calendar.getInstance();
        Encounter e1 = new Encounter();
        e1.setDate(cal.getTime());
        Encounter e2 = new Encounter();
        cal.add(Calendar.DAY_OF_MONTH, -5);
        e2.setDate(cal.getTime());

        Mockito.when(EncounterDao.find(Mockito.anyMap())).thenReturn(Arrays.asList(new Encounter[]{e2, e1}));

        assertEquals(member.getLastEncounter(), e1);
    }
}
