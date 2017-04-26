package org.watsi.uhp.managers;

import android.app.Application;
import android.util.Log;

import com.rollbar.android.Rollbar;
import com.squareup.leakcanary.LeakCanary;

import org.watsi.uhp.BuildConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExceptionManager {
    public static void init(Application application) {
        if (BuildConfig.REPORT_TO_ROLLBAR) {
            Rollbar.init(
                    application,
                    BuildConfig.ROLLBAR_API_KEY,
                    BuildConfig.ROLLBAR_ENV_KEY
            );
        }

        if (LeakCanary.isInAnalyzerProcess(application)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(application);
    }


    public static void requestFailure(String description, Request request, Response response, Map<String,String> params) {
        params.put("Url", request.url().toString());
        params.put("Method", request.method());
        RequestBody body = request.body();
        if (body != null) {
            if (body.contentType() != null) params.put("Content-Type", request.body().contentType().toString());
            try {
                params.put("Content-Length", String.valueOf(request.body().contentLength()));
            } catch (IOException e) {
                ExceptionManager.reportException(e);
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

    public static void reportException(Throwable e) {
        if (BuildConfig.REPORT_TO_ROLLBAR) {
            Rollbar.reportException(e);
        } else {
            Log.e("Exception", e.getMessage());
        }
    }

    public static void reportMessage(String message, String level, Map<String, String> params) {
        if (BuildConfig.REPORT_TO_ROLLBAR) {
            Rollbar.reportMessage(message, level, params);
        } else {
            Log.i("Message", message);
        }
    }

    public static void reportMessage(String message) {
        reportMessage(message, "info", null);
    }

    public static void setPersonData(String id, String username) {
        if (BuildConfig.REPORT_TO_ROLLBAR) {
            Rollbar.setPersonData(id, username, null);
        } else {
            Log.i("User logged in", "id:" + id + ", username:" + username);
        }
    }
}
