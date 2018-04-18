package org.watsi.uhp.repositories

import org.watsi.uhp.database.DatabaseHelper
import org.watsi.uhp.database.DiagnosisDao
import org.watsi.uhp.models.Diagnosis

class DiagnosisRepositoryImpl : DiagnosisRepository {

    override fun all(): List<Diagnosis> {
        return DatabaseHelper.fetchDao(Diagnosis::class.java).queryForAll() as List<Diagnosis>
    }

    override fun createOrUpdate(diagnosis: Diagnosis) {
        DatabaseHelper.fetchDao(Diagnosis::class.java).createOrUpdate(diagnosis)
    }

    override fun destroy(diagnosis: Diagnosis) {
        DatabaseHelper.fetchDao(Diagnosis::class.java).delete(diagnosis)
    }

    override fun fuzzySearchByName(query: String): List<Diagnosis> {
        return DiagnosisDao.searchByFuzzyDescriptionAndSearchAlias(query)
    }
}
