package org.watsi.uhp.helpers

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

object NetworkErrorHelper {

    fun isHttpUnauthorized(e: Throwable): Boolean {
        return e is HttpException && e.code() == 401
    }

    /**
     * Intended errors matched:
     * - java.net.UnknownHostException: Unable to resolve host <hostname>: No address associated with hostname
     */
    fun isPhoneOfflineError(e: Throwable): Boolean {
        val message = e.message.orEmpty()
        return (e is UnknownHostException && message.contains("Unable to resolve host"))
    }

    /**
     * Intended errors matched:
     * - java.net.ConnectException: Failed to connect to <server name>
     * - java.net.NoRouteToHostException: No route to host
     * - java.io.IOException: unexpected end of stream on Connection <connection>
     */
    fun isServerOfflineError(e: Throwable): Boolean {
        val message = e.message.orEmpty()
        return (e is ConnectException ||
                e is NoRouteToHostException ||
                (e is IOException && message.contains("unexpected end of stream")))
    }

    /**
     * Intended errors matched:
     * - java.net.SocketException: Network is unreachable
     * - java.net.SocketException: Connection timed out
     * - java.net.SocketException: Software caused connection abort
     * - java.net.SocketTimeoutException: timeout
     * - java.net.SocketTimeoutException: connect timed out
     * - java.net.SocketTimeoutException: SSL handshake timed out
     * - javax.net.ssl.SSLException: Write error: ssl=<hex>: I/O error during system call, Software caused connection abort
     * - javax.net.ssl.SSLException: Read error: ssl=<hex>: I/O error during system call, Software caused connection abort
     * - javax.net.ssl.SSLHandshakeException: SSL handshake aborted: ssl=<hex>: I/O error during system call, Connection timed out
     */
    fun isPoorConnectivityError(e: Throwable): Boolean {
        val message = e.message.orEmpty()
        return ((e is SocketException && message.findAnyOf(listOf("unreachable", "timed out", "connection abort")) != null) ||
                e is SocketTimeoutException ||
                (e is SSLException && message.contains("connection abort")) ||
                (e is SSLHandshakeException && message.contains("timed out")))
    }
}
