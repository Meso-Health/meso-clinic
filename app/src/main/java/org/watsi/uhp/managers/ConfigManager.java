package org.watsi.uhp.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ConfigManager {
    private final static String MEMBERS_LAST_MODIFIED_PREF_KEY = "members_last_modified";
    private final static String BILLABLES_LAST_MODIFIED_PREF_KEY = "billables_last_modified";
    public final static String TOKEN_PREFERENCES_KEY = "token";

    public static void setMemberLastModified(String lastModifiedTimestamp, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(MEMBERS_LAST_MODIFIED_PREF_KEY, lastModifiedTimestamp);
        editor.apply();
    }

    public static String getMemberLastModified(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(MEMBERS_LAST_MODIFIED_PREF_KEY, null);
    }

    public static void setBillablesLastModified(String lastModifiedTimestamp, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(BILLABLES_LAST_MODIFIED_PREF_KEY, lastModifiedTimestamp);
        editor.apply();
    }

    public static String getBillablesLastModified(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(BILLABLES_LAST_MODIFIED_PREF_KEY, null);
    }

    public static void setLoggedInUserToken(String token, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN_PREFERENCES_KEY, token);
        editor.apply();
    }

    public static String getLoggedInUserToken(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(TOKEN_PREFERENCES_KEY, null);
    }
}
