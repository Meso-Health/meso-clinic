package org.watsi.uhp.managers;

import java.util.Calendar;
import java.util.Date;

/**
 * Provides a single interface for accessing the current time
 * and a means to overwrite time for testing purposes
 */
public class Clock {

    private static Date mStaticTime = null;

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
}
