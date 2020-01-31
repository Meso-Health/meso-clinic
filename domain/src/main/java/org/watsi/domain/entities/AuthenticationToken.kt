package org.watsi.domain.entities

import org.threeten.bp.Instant

data class AuthenticationToken(val token: String, val expiresAt: Instant, val user: User) {

    fun getHeaderString() = "Token $token"
}
