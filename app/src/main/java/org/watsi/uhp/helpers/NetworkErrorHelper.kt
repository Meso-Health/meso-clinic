package org.watsi.uhp.helpers

import retrofit2.HttpException

object NetworkErrorHelper {
    fun isHttpUnauthorized(e: Throwable): Boolean {
        return e is HttpException && e.code() == 401
    }

    fun isPhoneOfflineError(e: Throwable): Boolean {
        return e is RuntimeException && e.message.orEmpty().contains("Unable to resolve host")
    }

    fun isServerOfflineError(e: Throwable): Boolean {
        return e is RuntimeException && e.message.orEmpty().contains("unexpected end of stream")
    }

    fun isPoorConnectivityError(e: Throwable): Boolean {
        return e is RuntimeException && e.message.orEmpty().contains("timeout")
    }
}
