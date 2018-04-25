package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.watsi.domain.entities.AuthenticationToken

data class AuthenticationTokenApi(val token: String,
                                  @SerializedName("expires_at") val expiresAt: String,
                                  val user: UserApi) {

    fun toAuthenticationToken(): AuthenticationToken {
        return AuthenticationToken(token, expiresAt, user.toUser())
    }
}
