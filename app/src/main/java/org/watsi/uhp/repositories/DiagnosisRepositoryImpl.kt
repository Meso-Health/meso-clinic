package org.watsi.uhp.repositories

import org.watsi.uhp.database.DiagnosisDao
import org.watsi.uhp.models.Diagnosis

class DiagnosisRepositoryImpl : DiagnosisRepository {
    override fun fuzzySearchByName(query: String): List<Diagnosis> {
        return DiagnosisDao.searchByFuzzyDescriptionAndSearchAlias(query)
    }
}
