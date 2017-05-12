package org.watsi.uhp.api;

import android.accounts.AccountManager;

import org.watsi.uhp.managers.Authenticator;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Handles invalidating the current account's auth token if an
 * unauthorized response is returned
 */
class UnauthorizedInterceptor implements Interceptor {

    private final AccountManager mAccountManager;

    public UnauthorizedInterceptor(AccountManager accountManager)  {
        this.mAccountManager = accountManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (response.code() == 401) {
            String token = request.header(UhpApi.AUTHORIZATION_HEADER);
            mAccountManager.invalidateAuthToken(Authenticator.ACCOUNT_TYPE, token);
        }
        return response;
    }
}
