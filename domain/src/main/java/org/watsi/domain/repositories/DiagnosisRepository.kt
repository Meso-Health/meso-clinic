package org.watsi.domain.repositories

import org.watsi.domain.entities.Diagnosis

interface DiagnosisRepository {
    fun all(): List<Diagnosis>
    fun fetch()
}
