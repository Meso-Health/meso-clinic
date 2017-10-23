package org.watsi.uhp.api;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class NotModifiedInterceptor implements Interceptor {
    @Override public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        Response response = chain.proceed(request);

        // Retrofit interprets 304 network responses as 200. But we still want to return 304.
        // This solution is highlighted in this post below:
        // https://android.jlelse.eu/reducing-your-networking-footprint-with-okhttp-etags-and-if-modified-since-b598b8dd81a1
        if (response.isSuccessful() && response.networkResponse() != null &&
                response.networkResponse().code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            return response.newBuilder().code(304).build();
        } else {
            return response;
        }
    }
}
