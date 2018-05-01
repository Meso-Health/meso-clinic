package org.watsi.uhp.managers

import com.rollbar.android.Rollbar
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.User

class RollbarLogger : Logger {

    companion object {
        private val MESSAGE_LEVEL_WARNING = "warning"
        private val MESSAGE_LEVEL_INFO = "info"
        private val MESSAGE_LEVEL_ERROR = "error"
    }

    override fun setUser(user: User) {
        Rollbar.setPersonData(user.id.toString(), user.username, null)
    }

    override fun clearUser() {
        Rollbar.setPersonData(null)
    }

    private fun reportMessage(message: String, messageLevel: String, params: Map<String, String>) {
        if (params.isEmpty()) {
            Rollbar.reportMessage(message, messageLevel)
        } else {
            Rollbar.reportMessage(message, messageLevel, params)
        }
    }

    override fun info(message: String, params: Map<String, String>) {
        reportMessage(message, MESSAGE_LEVEL_INFO, params)
    }

    override fun warning(message: String, params: Map<String, String>) {
        reportMessage(message, MESSAGE_LEVEL_WARNING, params)
    }

    override fun warning(throwable: Throwable, params: Map<String, String>) {
        Rollbar.reportException(throwable, MESSAGE_LEVEL_WARNING, throwable.message, params)
    }

    override fun error(message: String, params: Map<String, String>) {
        reportMessage(message, MESSAGE_LEVEL_ERROR, params)
    }

    override fun error(throwable: Throwable, params: Map<String, String>) {
        Rollbar.reportException(throwable, MESSAGE_LEVEL_ERROR, throwable.message, params)
    }
}
