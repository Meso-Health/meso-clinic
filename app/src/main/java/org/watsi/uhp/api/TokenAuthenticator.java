package org.watsi.uhp.api;

import android.content.Context;
import android.preference.PreferenceManager;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 *  Authenticator for using an authentication token accessing the API
 */
public class TokenAuthenticator implements Authenticator {

    public final static String TOKEN_PREFERENCES_KEY = "token";

    private final Context context;

    public TokenAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        String authToken = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(TOKEN_PREFERENCES_KEY, null);
        return response.request()
                .newBuilder()
                .header("Authorization", "Token " + authToken)
                .build();
    }
}
