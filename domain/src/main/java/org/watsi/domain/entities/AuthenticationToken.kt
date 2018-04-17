package org.watsi.domain.entities

data class AuthenticationToken(val token: String, val expiresAt: String, val user: User) {

    fun getHeaderString() = "Token $token"
}
