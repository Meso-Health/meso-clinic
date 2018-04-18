package org.watsi.uhp.repositories

import org.watsi.uhp.models.Diagnosis

interface DiagnosisRepository {
    fun all(): List<Diagnosis>
    fun createOrUpdate(diagnosis: Diagnosis)
    fun destroy(diagnosis: Diagnosis)
    fun fuzzySearchByName(query: String): List<Diagnosis>
}
