package org.watsi.device.api.models

import org.watsi.domain.entities.Diagnosis

data class DiagnosisApi(
    val id: Int,
    val description: String,
    val searchAliases: List<String>,
    val active: Boolean?
) {

    constructor (diagnosis: Diagnosis) : this(
        id = diagnosis.id,
        description = diagnosis.description,
        searchAliases = diagnosis.searchAliases,
        active = diagnosis.active
    )

    fun toDiagnosis(): Diagnosis {
        return Diagnosis(
            id = id,
            description = description,
            searchAliases = searchAliases,
            active = active ?: true // active defaults to true so that we don't need to wait for the corresponding backend changes.
        )
    }
}
