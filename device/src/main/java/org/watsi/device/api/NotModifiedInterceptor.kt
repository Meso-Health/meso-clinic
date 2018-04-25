package org.watsi.device.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.HttpURLConnection

class NotModifiedInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val response = chain.proceed(request)

        // Retrofit interprets 304 network responses as 200. But we still want to return 304.
        // This solution is highlighted in this post below:
        // https://android.jlelse.eu/reducing-your-networking-footprint-with-okhttp-etags-and-if-modified-since-b598b8dd81a1
        return if (response.isSuccessful && response.networkResponse() != null &&
                response.networkResponse()!!.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            response.newBuilder().code(304).build()
        } else {
            response
        }
    }
}
