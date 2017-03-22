package org.watsi.uhp.managers;

import com.rollbar.android.Rollbar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationManager {
    public static void requestFailure(String description, Request request, Response response, Map<String,String> params) {
        params.put("Url", request.url().toString());
        params.put("Method", request.method());
        RequestBody body = request.body();
        if (body != null) {
            if (body.contentType() != null) params.put("Content-Type", request.body().contentType().toString());
            try {
                params.put("Content-Length", String.valueOf(request.body().contentLength()));
            } catch (IOException e) {
                Rollbar.reportException(e);
            }
        }
        if (response != null) {
            params.put("X-Request-Id", response.header("X-Request-Id"));
            params.put("response.code", String.valueOf(response.code()));
            params.put("response.message", response.message());
        }
        Rollbar.reportMessage(description, "warning", params);
    }

    public static void requestFailure(String description, Request request, Response response) {
        requestFailure(description, request, response, new HashMap<String,String>());
    }
}
