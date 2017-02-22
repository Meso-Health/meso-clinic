package org.watsi.uhp.api;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 *  Authenticator for requesting an authentication token
 */
class BasicAuthenticator implements Authenticator {

    private final String username;
    private final String password;

    BasicAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        String basicAuthToken = Credentials.basic(username, password);
        return response.request()
                .newBuilder()
                .header("Authorization", basicAuthToken)
                .build();
    }
}
