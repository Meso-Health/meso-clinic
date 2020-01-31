package org.watsi.domain.entities

import java.io.Serializable
import java.util.UUID

data class LabResult(
    val id: UUID,
    val result: String,
    val encounterItemId: UUID
) : Serializable {
    companion object {
        fun malariaTestResults() : List<String> {
            return listOf(
                "positive",
                "negative",
                "unspecified"
            )
        }
    }
}
