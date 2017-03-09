package org.watsi.uhp.managers;

import com.rollbar.android.Rollbar;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class NotificationManager {
    public static void requestFailure(String description, Request request, Response response, Map<String,String> params) {
        params.put("Url", request.url().toString());
        params.put("Method", request.method());
        params.put("Host", request.header("Host"));
        params.put("X-Request-Id", request.header("X-Request-Id"));
        params.put("X-Request-Start", request.header("X-Request-Start"));
        params.put("Content-Length", request.header("Content-Length"));
        params.put("Content-Type", request.header("Content-Type"));
        if (response != null) {
            params.put("response.code", String.valueOf(response.code()));
            params.put("response.message", response.message());
        }
        Rollbar.reportMessage(description, "warning", params);
    }

    public static void requestFailure(String description, Request request, Response response) {
        requestFailure(description, request, response, new HashMap<String,String>());
    }
}
