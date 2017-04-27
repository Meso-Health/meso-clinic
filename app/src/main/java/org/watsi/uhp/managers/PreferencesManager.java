package org.watsi.uhp.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferencesManager {

    private final static String MEMBERS_LAST_MODIFIED_PREF_KEY = "membersLastModified";
    private final static String BILLABLES_LAST_MODIFIED_PREF_KEY = "billablesLastModified";
    private final static String USERNAME_PREF_KEY = "username";

    private final SharedPreferences mSharedPreferences;
    private final SharedPreferences.Editor mEditor;

    public PreferencesManager(Context context) {
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mEditor = mSharedPreferences.edit();
    }

    public void setMemberLastModified(String lastModifiedTimestamp) {
        setValue(MEMBERS_LAST_MODIFIED_PREF_KEY, lastModifiedTimestamp);
    }

    public String getMemberLastModified() {
        return getValue(MEMBERS_LAST_MODIFIED_PREF_KEY);
    }

    public void setBillablesLastModified(String lastModifiedTimestamp) {
        setValue(BILLABLES_LAST_MODIFIED_PREF_KEY, lastModifiedTimestamp);
    }

    public String getBillablesLastModified() {
        return getValue(BILLABLES_LAST_MODIFIED_PREF_KEY);
    }

    public String getUsername() {
        return getValue(USERNAME_PREF_KEY);
    }

    public void setUsername(String loggedInUsername) {
        setValue(USERNAME_PREF_KEY, loggedInUsername);
    }

    public void clearUsername() {
        clearValue(USERNAME_PREF_KEY);
    }

    private void setValue(String key, String value) {
        mEditor.putString(key, value);
        mEditor.apply();
    }

    private String getValue(String key) {
        return mSharedPreferences.getString(key, null);
    }

    private void clearValue(String key) {
        mEditor.remove(key);
        mEditor.apply();
    }
}
