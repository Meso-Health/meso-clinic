package org.watsi.uhp.api;

import org.watsi.uhp.managers.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 *  Interceptor for including the current user API token authorization for all fetch API requests
 */
public class TokenInterceptor implements Interceptor {

    private final SessionManager mSessionManager;

    TokenInterceptor(SessionManager sessionManager) {
        this.mSessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        String method = request.method();

        if (method.equals("GET")) {
            String authToken = mSessionManager.getToken();
            Request requestWithAuth = chain.request()
                    .newBuilder()
                    .header(UhpApi.AUTHORIZATION_HEADER, "Token " + authToken)
                    .build();
            return chain.proceed(requestWithAuth);
        } else {
            return chain.proceed(chain.request());
        }
    }
}
