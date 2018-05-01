package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.db.daos.DiagnosisDao
import org.watsi.device.db.models.DiagnosisModel
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.repositories.DiagnosisRepository

class DiagnosisRepositoryImpl(private val diagnosisDao: DiagnosisDao,
                              private val api: CoverageApi,
                              private val sessionManager: SessionManager,
                              private val preferencesManager: PreferencesManager,
                              private val clock: Clock) : DiagnosisRepository {
    override fun all(): Single<List<Diagnosis>> {
        return diagnosisDao.all()
                .map { it.map { it.toDiagnosis() } }
                .subscribeOn(Schedulers.io())
    }

    private fun save(diagnosis: Diagnosis): Completable {
        return Completable.fromAction {
            if (diagnosisDao.find(diagnosis.id) != null) {
                diagnosisDao.update(DiagnosisModel.fromDiagnosis(diagnosis, clock))
            } else {
                diagnosisDao.insert(DiagnosisModel.fromDiagnosis(diagnosis, clock))
            }
        }
    }

    override fun fetch(): Completable {
        val token = sessionManager.currentToken()
        return if (token == null) {
            Completable.complete()
        } else {
            api.diagnoses(token.getHeaderString()).flatMapCompletable { updatedDiagnoses ->
                // TODO: more efficient way of saving?
                // TODO: clean up any diagnoses not returned in the fetch
                Completable.concat(updatedDiagnoses.map { save(it.toDiagnosis()) })
            }.andThen {
                preferencesManager.updateDiagnosesLastFetched(clock.instant())
            }
        }
    }
}
