package org.watsi.uhp.managers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Provides a single interface for accessing the current time
 * and a means to overwrite time for testing purposes
 */
public class Clock {

    private static Date mStaticTime = null;
    public static String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ";

    public static void setTime(Date time) {
        mStaticTime = time;
    }

    public static Date getCurrentTime() {
        if (mStaticTime == null) {
            return Calendar.getInstance().getTime();
        } else {
            return mStaticTime;
        }
    }

    public static String asIso(Date date) {
        return new SimpleDateFormat(ISO_DATE_FORMAT).format(date);
    }
}
