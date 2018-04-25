package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.watsi.domain.entities.User

data class UserApi(val id: Int,
                   val username: String,
                   val name: String,
                   @SerializedName("provider_id") val providerId: Int) {

    fun toUser(): User {
        return User(id, username, name, providerId)
    }
}
