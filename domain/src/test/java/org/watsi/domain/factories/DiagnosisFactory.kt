package org.watsi.domain.factories

import org.watsi.domain.entities.Diagnosis

object DiagnosisFactory {

    fun build(
        id: Int = 1,
        description: String = "Malaria",
        searchAliases: List<String> = listOf("Mal"),
        active: Boolean = true
    ) : Diagnosis {
        return Diagnosis(
            id = id,
            description = description,
            searchAliases = searchAliases,
            active = active
        )
    }
}
