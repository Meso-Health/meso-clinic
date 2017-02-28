package org.watsi.uhp.api;

import android.content.Context;
import android.preference.PreferenceManager;

import org.watsi.uhp.managers.ConfigManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 *  Interceptor for including the current user API token authorization for all fetch API requests
 */
public class TokenInterceptor implements Interceptor {

    private final Context context;

    TokenInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        String method = request.method();

        if (method.equals("GET")) {
            String authToken = PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getString(ConfigManager.TOKEN_PREFERENCES_KEY, null);
            Request requestWithAuth = chain.request()
                    .newBuilder()
                    .header("Authorization", "Token " + authToken)
                    .build();
            return chain.proceed(requestWithAuth);
        } else {
            return chain.proceed(chain.request());
        }
    }
}
