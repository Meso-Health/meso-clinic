package org.watsi.domain.entities

data class User(
        val id: Int,
        val username: String,
        val name: String,
        val role: Role,
        val providerId: Int
) {
    enum class Role { ENROLLMENT_WORKER, PROVIDER }
}
