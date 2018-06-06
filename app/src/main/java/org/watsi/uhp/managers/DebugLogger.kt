package org.watsi.uhp.managers

import android.util.Log
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.User

class DebugLogger : Logger {

    companion object {
        private const val TAG = "watsi"
    }

    override fun setUser(user: User) {
        Log.i(TAG, "setUser - " + user.toString())
    }

    override fun clearUser() {
        Log.i(TAG, "clearUser")
    }

    override fun info(message: String, params: Map<String, String>) {
        Log.i(TAG, logMessage(message, params))
    }

    override fun warning(message: String, params: Map<String, String>) {
        Log.w(TAG, logMessage(message, params))
    }

    override fun warning(throwable: Throwable, params: Map<String, String>) {
        Log.e(TAG, logMessage(throwable.localizedMessage, params))
    }

    override fun error(message: String, params: Map<String, String>) {
        Log.e(TAG, logMessage(message, params))
    }

    override fun error(throwable: Throwable, params: Map<String, String>) {
        Log.e(TAG, logMessage(throwable.localizedMessage, params), throwable)
    }

    private fun logMessage(message: String?, params: Map<String, String>): String? {
        return if (params.isEmpty()) message else message + " - " + params.toString()
    }
}
