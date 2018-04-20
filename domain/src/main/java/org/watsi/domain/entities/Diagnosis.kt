package org.watsi.domain.entities

data class Diagnosis(val id: Int = 0,
                     val description: String,
                     val searchAliases: List<String>)
