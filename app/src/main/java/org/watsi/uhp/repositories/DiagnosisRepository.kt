package org.watsi.uhp.repositories

import org.watsi.uhp.models.Diagnosis

interface DiagnosisRepository {
    fun fuzzySearchByName(query: String): List<Diagnosis>
}
