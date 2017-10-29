package org.watsi.uhp.helpers;

import android.support.annotation.NonNull;

public class StringUtils {
    public static String titleCase(@NonNull String string) {
        return string.substring(0,1) + string.substring(1).toLowerCase();
    }
}
