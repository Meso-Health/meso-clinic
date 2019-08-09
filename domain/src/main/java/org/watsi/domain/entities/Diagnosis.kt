package org.watsi.domain.entities

import java.io.Serializable

data class Diagnosis(val id: Int = 0,
                     val description: String,
                     val searchAliases: List<String>,
                     val active: Boolean) : Serializable
