package org.watsi.device.db.repositories

import me.xdrop.fuzzywuzzy.FuzzySearch
import org.threeten.bp.Clock
import org.watsi.device.db.daos.DiagnosisDao
import org.watsi.device.db.models.DiagnosisModel
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.repositories.DiagnosisRepository

class DiagnosisRepositoryImpl(private val diagnosisDao: DiagnosisDao,
                              private val clock: Clock) : DiagnosisRepository {

    override fun all(): List<Diagnosis> {
        return diagnosisDao.all().map { it.toDiagnosis() }
    }

    override fun createOrUpdate(diagnosis: Diagnosis) {
        diagnosisDao.insert(DiagnosisModel.fromDiagnosis(diagnosis, clock))
        // TODO: handle updating if it already exists
    }

    override fun destroy(diagnosis: Diagnosis) {
        diagnosisDao.delete(DiagnosisModel.fromDiagnosis(diagnosis, clock))
    }

    override fun fuzzySearchByName(query: String): List<Diagnosis> {
        val topMatchingDescriptions = FuzzySearch.extractTop(
                query, diagnosisDao.uniqueDescriptions(), 6, 60)

        // This sorts the fuzzy search results by decreasing score, increasing alphabetical order.
        topMatchingDescriptions.sortWith(Comparator { o1, o2 ->
            if (o2.score == o1.score)
                o1.string.compareTo(o2.string)
            else
                Integer.compare(o2.score, o1.score)
        })

        val matchingSearchAliasesDiagnoses = diagnosisDao.searchAliasLike("%$query%")
        val matchingDescriptionDiagnoses = topMatchingDescriptions.map {
            diagnosisDao.findByDescription(it.string)
        }.flatten()

        return (matchingSearchAliasesDiagnoses + matchingDescriptionDiagnoses)
                .distinct()
                .map { it.toDiagnosis() }
    }
}
