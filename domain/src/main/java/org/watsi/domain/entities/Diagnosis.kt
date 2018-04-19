package org.watsi.domain.entities

import java.util.UUID

data class Diagnosis(val id: UUID,
                     val description: String,
                     val searchAliases: List<String>)
