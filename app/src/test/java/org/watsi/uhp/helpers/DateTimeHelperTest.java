package org.watsi.uhp.helpers;

import org.junit.Test;

import java.util.Calendar;

import static junit.framework.Assert.assertEquals;

public class DateTimeHelperTest {
    
    @Test
    public void makeCalendarToday() throws Exception {
        Calendar calendar = DateTimeHelper.makeCalendarToday();
        assertEquals(calendar.get(Calendar.HOUR), 0);
        assertEquals(calendar.get(Calendar.MINUTE), 0);
        assertEquals(calendar.get(Calendar.SECOND), 0);
        assertEquals(calendar.get(Calendar.MILLISECOND), 0);
    }
}
