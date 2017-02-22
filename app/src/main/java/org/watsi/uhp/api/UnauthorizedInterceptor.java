package org.watsi.uhp.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Handles re-authenticating to the back-end if an
 * unauthorized response is returned
 */
class UnauthorizedInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (response.code() == 401) {
            // TODO: should prompt login and halt requests
        }
        return response;
    }
}
