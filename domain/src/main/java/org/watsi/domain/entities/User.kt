package org.watsi.domain.entities

import java.io.Serializable

data class User(
    val id: Int,
    val username: String,
    val name: String,
    val providerId: Int,
    val providerType: ProviderType?,
    val role: String,
    val securityPin: String
): Serializable {

    enum class ProviderType {
        HEALTH_CENTER,
        PRIMARY_HOSPITAL,
        GENERAL_HOSPITAL,
        TERTIARY_HOSPITAL,
        UNCLASSIFIED
    }
}
