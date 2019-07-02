package org.watsi.uhp.managers

import com.rollbar.android.Rollbar
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.User

class RollbarLogger : Logger {

    override fun setUser(user: User) {
        Rollbar.instance().setPersonData(user.id.toString(), user.username, null)
    }

    override fun clearUser() {
        Rollbar.instance().clearPersonData()
    }

    override fun info(message: String, params: Map<String, String>) {
        Rollbar.instance().info(message, params)
    }

    override fun info(throwable: Throwable, params: Map<String, String>) {
        Rollbar.instance().info(throwable, params, throwable.message)
    }

    override fun warning(message: String, params: Map<String, String>) {
        Rollbar.instance().warning(message, params)
    }

    override fun warning(throwable: Throwable, params: Map<String, String>) {
        Rollbar.instance().warning(throwable, params, throwable.message)
    }

    override fun error(message: String, params: Map<String, String>) {
        Rollbar.instance().error(message, params)
    }

    override fun error(throwable: Throwable, params: Map<String, String>) {
        Rollbar.instance().error(throwable, params, throwable.message)
    }
}
