package org.watsi.device.db.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.watsi.device.api.CoverageApi
import org.watsi.device.db.daos.EnrollmentPeriodDao
import org.watsi.device.db.models.EnrollmentPeriodModel
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.EnrollmentPeriodRepository
import org.watsi.domain.utils.DateUtils

class EnrollmentPeriodRepositoryImpl(
    private val enrollmentPeriodDao: EnrollmentPeriodDao,
    private val api: CoverageApi,
    private val sessionManager: SessionManager,
    private val clock: Clock
) : EnrollmentPeriodRepository {
    override fun fetch(): Completable {
        return sessionManager.currentAuthenticationToken()?.let { token ->
            Completable.fromCallable {
                val enrollmentPeriods = api.getEnrollmentPeriods(token.getHeaderString())
                        .blockingGet().map { it.toEnrollmentPeriod() }
                enrollmentPeriodDao.upsert(
                    enrollmentPeriods.map { EnrollmentPeriodModel.fromEnrollmentPeriod(it, clock) })
            }.subscribeOn(Schedulers.io())
        } ?: Completable.error(Exception("Current token is null while calling EnrollmentPeriodRepository.fetch"))
    }

    override fun shouldEnroll(member: Member): Single<Boolean> {
        return Single.fromCallable {
            val currentDate = DateUtils.instantToLocalDate(clock.instant(), clock)
            val enrollmentPeriods = enrollmentPeriodDao.active(currentDate).blockingGet().map { it.toEnrollmentPeriod() }
            // hack to avoid having to fetch and join via administrative divisions
            val isActiveEnrollments = enrollmentPeriods.isNotEmpty()
            val memberHasEnrolled = enrollmentPeriods.any { it.coverageEndDate == member.coverageEndDate }
            isActiveEnrollments && !memberHasEnrolled
        }.subscribeOn(Schedulers.io())
    }
}
