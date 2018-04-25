package org.watsi.domain.repositories

import org.watsi.domain.entities.Diagnosis

interface DiagnosisRepository {
    fun fetch()
    fun fuzzySearchByName(query: String): List<Diagnosis>
}
