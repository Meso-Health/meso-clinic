package org.watsi.uhp.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;

public class PreferencesManager {
    private final static SimpleDateFormat LAST_MODIFIED_DATE_FORMAT = new SimpleDateFormat("hh:mm:ss a  yyyy/M/d");
    private final static String MEMBERS_LAST_MODIFIED_PREF_KEY = "membersLastModified";
    private final static String BILLABLES_LAST_MODIFIED_PREF_KEY = "billablesLastModified";
    private final static String DIAGNOSES_LAST_MODIFIED_PREF_KEY = "diagnosesLastModified";
    private final static String USERNAME_PREF_KEY = "username";

    private final SharedPreferences mSharedPreferences;
    private final SharedPreferences.Editor mEditor;

    public PreferencesManager(Context context) {
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mEditor = mSharedPreferences.edit();
    }

    public void updateMembersLastModified() {
        setValue(MEMBERS_LAST_MODIFIED_PREF_KEY, LAST_MODIFIED_DATE_FORMAT.format(Clock.getCurrentTime()));
    }

    public String getMemberLastModified() {
        return getValue(MEMBERS_LAST_MODIFIED_PREF_KEY);
    }

    public void updateBillableLastModified() {
        setValue(BILLABLES_LAST_MODIFIED_PREF_KEY, LAST_MODIFIED_DATE_FORMAT.format(Clock.getCurrentTime()));
    }

    public void updateDiagnosesLastModified() {
        setValue(DIAGNOSES_LAST_MODIFIED_PREF_KEY, LAST_MODIFIED_DATE_FORMAT.format(Clock.getCurrentTime()));
    }

    public String getBillablesLastModified() {
        return getValue(BILLABLES_LAST_MODIFIED_PREF_KEY);
    }

    public void setDiagnosesLastModified(String lastModifiedTimestamp) {
        setValue(DIAGNOSES_LAST_MODIFIED_PREF_KEY, lastModifiedTimestamp);
    }

    public String getDiagnosesLastModified() {
        return getValue(DIAGNOSES_LAST_MODIFIED_PREF_KEY);
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
