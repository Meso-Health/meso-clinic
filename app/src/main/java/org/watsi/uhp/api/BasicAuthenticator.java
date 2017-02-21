package org.watsi.uhp.api;

import android.content.Context;

import org.watsi.uhp.managers.ConfigManager;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 *  Authenticator for requesting an authentication token
 */
public class BasicAuthenticator implements Authenticator {

    private final Context context;

    public BasicAuthenticator(Context context) {
        this.context = context;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        String basicAuthToken = Credentials.basic(
                ConfigManager.getApiUsername(context),
                ConfigManager.getApiPassword(context)
        );
        return response.request()
                .newBuilder()
                .header("Authorization", basicAuthToken)
                .build();
    }
}
