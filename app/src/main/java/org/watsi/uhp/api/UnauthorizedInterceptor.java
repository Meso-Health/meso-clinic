package org.watsi.uhp.api;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Handles re-authenticating to the back-end if an
 * unauthorized response is returned
 */
public class UnauthorizedInterceptor implements Interceptor {

    private final Context context;
    private int UNAUTHORIZED_RESPONSE_CODE = 401;

    protected UnauthorizedInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (response.code() == UNAUTHORIZED_RESPONSE_CODE) {
            if (ApiService.refreshApiToken(context)) {
                // successfully re-authenticated, so retry original request
                return response.newBuilder().build();
            } else {
                // TODO: failed to re-authenticate
                return response;
            }
        } else {
            return response;
        }
    }
}
