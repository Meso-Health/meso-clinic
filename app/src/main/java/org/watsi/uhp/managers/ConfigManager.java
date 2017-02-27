package org.watsi.uhp.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Static method for reading config settings stored
 * in the res/xml/config.xml file
 */
public class ConfigManager {
    private final static String ROLLBAR_API_KEY = "ROLLBAR_API_KEY";
    private final static String API_HOST = "API_HOST";
    private final static String FACILITY_ID = "FACILITY_ID";
    private final static String MEMBERS_LAST_MODIFIED_PREF_KEY = "members_last_modified";
    private final static String BILLABLES_LAST_MODIFIED_PREF_KEY = "billables_last_modified";
    public final static String TOKEN_PREFERENCES_KEY = "token";

    public static String getRollbarApiKey(Context context) {
        return getConfigValue(ROLLBAR_API_KEY, context);
    }

    public static String getApiHost(Context context) {
        return getConfigValue(API_HOST, context);
    }

    public static int getFacilityId(Context context) {
        String facilityId = getConfigValue(FACILITY_ID, context);
        if (facilityId == null) {
            return 0;
        } else {
            return Integer.parseInt(facilityId);
        }
    }

    private static String getConfigValue(String key, Context context) {
        return readConfig(context).get(key);
    }

    private static Map<String,String> readConfig(Context context) {
        Map<String,String> configMap = new HashMap<>();
        int eventType = -1;
        Resources res = context.getResources();
        XmlResourceParser xrp = res.getXml(R.xml.secret);

        try {
            while(eventType != XmlResourceParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                    if (xrp.getName().equals("entry")) {
                        String key = xrp.getAttributeValue(null, "key");
                        xrp.next();
                        String value = xrp.getText();
                        configMap.put(key, value);
                    }
                }
                xrp.next();
                eventType = xrp.getEventType();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.w("UHP", e.getMessage());
            Rollbar.reportException(e);
        }
        return configMap;
    }

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
