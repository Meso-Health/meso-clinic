package org.watsi.domain.entities

import java.io.Serializable

data class User(
    val id: Int,
    val username: String,
    val name: String,
    val providerId: Int,
    val role: String,
    val securityPin: String
): Serializable
