package org.watsi.device.api.models

import org.watsi.domain.entities.User

data class UserApi(
    val id: Int,
    val username: String,
    val name: String,
    val providerId: Int,
    val securityPin: String
) {

    fun toUser(): User {
        return User(id, username, name, providerId, securityPin)
    }
}
