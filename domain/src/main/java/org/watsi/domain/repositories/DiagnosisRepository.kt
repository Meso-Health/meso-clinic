package org.watsi.domain.repositories

import org.watsi.domain.entities.Diagnosis

interface DiagnosisRepository {
    fun createOrUpdate(diagnosis: Diagnosis)
    fun destroy(diagnosis: Diagnosis)
    fun fuzzySearchByName(query: String): List<Diagnosis>
}
