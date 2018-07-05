package org.watsi.device.managers

import org.watsi.domain.entities.User

interface Logger {
    fun setUser(user: User)
    fun clearUser()

    fun info(message: String, params: Map<String, String> = HashMap())
    fun warning(message: String, params: Map<String, String> = HashMap())
    fun warning(throwable: Throwable, params: Map<String, String> = HashMap())
    fun error(message: String, params: Map<String, String> = HashMap())
    fun error(throwable: Throwable, params: Map<String, String> = HashMap())
}
