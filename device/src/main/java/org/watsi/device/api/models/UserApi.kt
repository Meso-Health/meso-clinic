package org.watsi.device.api.models

import org.watsi.domain.entities.User
import org.watsi.domain.entities.User.ProviderType

data class UserApi(
    val id: Int,
    val username: String,
    val name: String,
    val providerId: Int,
    val providerType: String?,
    val role: String,
    val securityPin: String
) {
    constructor(user: User): this(
        id = user.id,
        username = user.username,
        name = user.name,
        providerId = user.providerId,
        providerType = user.providerType?.toString()?.toLowerCase(),
        role = user.role,
        securityPin = user.securityPin
    )

    fun toUser(): User {
        return User(
            id = id,
            username = username,
            name = name,
            providerId = providerId,
            providerType = providerType?.let {ProviderType.valueOf(it.toUpperCase())},
            role = role,
            securityPin = securityPin
        )
    }
}
