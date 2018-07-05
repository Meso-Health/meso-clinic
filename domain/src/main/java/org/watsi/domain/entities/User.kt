package org.watsi.domain.entities

data class User(val id: Int,
                val username: String,
                val name: String,
                val providerId: Int)
