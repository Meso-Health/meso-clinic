package org.watsi.uhp.helpers

import retrofit2.HttpException

object NetworkErrorHelper {
    fun isHttpUnauthorized(e: Throwable): Boolean {
        return e is HttpException && e.code() == 401
    }

    fun isPhoneOfflineError(e: Throwable): Boolean {
        return e is RuntimeException && e.message.orEmpty().contains("Unable to resolve host")
    }

    /**
     * Intended errors caught:
     * - java.io.IOException: unexpected end of stream on Connection <connection>
     * - java.net.ConnectException: Failed to connect to <server name>
     * - java.net.NoRouteToHostException: No route to host
     * - all of the above chained / rethrown as RuntimeExceptions
     */
    fun isServerOfflineError(e: Throwable): Boolean {
        val message = e.message.orEmpty()
        return message.contains("unexpected end of stream") ||
                message.contains("Failed to connect to") ||
                message.contains("No route to host")
    }

    /**
     * Intended errors caught:
     * - java.net.SocketException: Network is unreachable
     * - java.net.SocketException: Connection timed out
     * - java.net.SocketException: Software caused connection abort
     * - java.net.SocketTimeoutException: timeout
     * - java.net.SocketTimeoutException: connect timed out
     * - java.net.SocketTimeoutException: SSL handshake timed out
     * - javax.net.ssl.SSLException: Write error: ssl=<hex>: I/O error during system call, Software caused connection abort
     * - javax.net.ssl.SSLException: Read error: ssl=<hex>: I/O error during system call, Software caused connection abort
     * - javax.net.ssl.SSLHandshakeException: SSL handshake aborted: ssl=<hex>: I/O error during system call, Connection timed out
     * - all of the above chained / rethrown as RuntimeExceptions
     */
    fun isPoorConnectivityError(e: Throwable): Boolean {
        val message = e.message.orEmpty()
        return message.contains("Network is unreachable") ||
                message.contains("timeout") ||
                message.contains("timed out") ||
                message.contains("connection abort")
    }
}
