package org.watsi.uhp.managers;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
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
    private static String ROLLBAR_API_KEY = "ROLLBAR_API_KEY";
    private static String API_HOST = "API_HOST";
    private static String FACILITY_ID = "FACILITY_ID";

    public static String getRollbarApiKey(Activity activity) {
        return getConfigValue(ROLLBAR_API_KEY, activity);
    }

    public static String getApiHost(Activity activity) {
        return getConfigValue(API_HOST, activity);
    }

    public static int getFacilityId(Activity activity) {
        return Integer.parseInt(getConfigValue(FACILITY_ID, activity));
    }

    private static String getConfigValue(String key, Activity activity) {
        String configValue = readConfig(activity).get(key);
        if (key == null) {
            throw new RuntimeException("must set ROLLBAR_API_KEY in res/xml/secret.xml");
        }
        return configValue;
    }

    private static Map<String,String> readConfig(Activity activity) {
        Map<String,String> configMap = new HashMap<>();
        Resources res = activity.getResources();
        XmlResourceParser xrp = res.getXml(R.xml.secret);

        int eventType = -1;
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
            Log.d("UHP", e.getMessage());
            Rollbar.reportException(e);
        }
        return configMap;
    }
}
