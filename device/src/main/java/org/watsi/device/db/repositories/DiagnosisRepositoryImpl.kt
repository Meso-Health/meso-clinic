package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.db.DbHelper
import org.watsi.device.db.daos.DiagnosisDao
import org.watsi.device.db.models.DiagnosisModel
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.repositories.DiagnosisRepository

class DiagnosisRepositoryImpl(
    private val diagnosisDao: DiagnosisDao,
    private val api: CoverageApi,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    private val clock: Clock
) : DiagnosisRepository {

    override fun allActive(): Single<List<Diagnosis>> {
        return diagnosisDao.allActive().map { it.map { it.toDiagnosis() } }.subscribeOn(Schedulers.io())
    }

    override fun delete(ids: List<Int>): Completable {
        return Completable.fromAction {
            ids.chunked(DbHelper.SQLITE_MAX_VARIABLE_NUMBER).map { diagnosisDao.delete(it) }
        }.subscribeOn(Schedulers.io())
    }

    override fun countActive(): Single<Int> {
        return diagnosisDao.countActive()
    }

    /**
     * Removes any persisted diagnoses that are not returned in the API results and overwrites
     * any persisted data if the API response contains updated data.
     */
    override fun fetch(): Completable {
        return sessionManager.currentAuthenticationToken()?.let { token ->
            Completable.fromAction {
                val serverDiagnoses = api.getDiagnoses(token.getHeaderString()).blockingGet()
                val serverDiagnosesIds = serverDiagnoses.map { it.id }
                val clientDiagnosesIds = diagnosisDao.allActive().blockingGet().map { it.id }
                val serverRemovedDiagnosesIds = clientDiagnosesIds.minus(serverDiagnosesIds)

                diagnosisDao.upsert(serverDiagnoses.map { diagnosisApi ->
                    DiagnosisModel.fromDiagnosis(diagnosisApi.toDiagnosis(), clock)
                })

                // Mark server removed diagnoses as inactive on the client side.
                val diagnosesToMarkAsInactive = diagnosisDao.findAll(serverRemovedDiagnosesIds).blockingGet()
                diagnosisDao.upsert(
                    diagnosisModels = diagnosesToMarkAsInactive.map { diagnosisModel ->
                        diagnosisModel.copy(active = false)
                    }
                )

                preferencesManager.updateDiagnosesLastFetched(clock.instant())
            }.subscribeOn(Schedulers.io())
        } ?: Completable.error(Exception("Current token is null while calling DiagnosisRepositoryImpl.fetch"))
    }
}
