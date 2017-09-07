package org.watsi.uhp.helpers;

import java.util.Calendar;

public class DateTimeHelper {
    public static Calendar makeCalendarToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        return cal;
    }

    public static Calendar makeCalendarThreeMonthsAgo() {
        Calendar threeMonthsAgo = makeCalendarToday();
        threeMonthsAgo.add(Calendar.MONTH, -3);
        return threeMonthsAgo;
    }

    public static Calendar makeCalendarTomorrow() {
        Calendar tomorrow = makeCalendarToday();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        return tomorrow;
    }
}
