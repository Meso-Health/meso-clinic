package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.device.db.daos.EnrollmentPeriodDao
import org.watsi.device.db.models.EnrollmentPeriodModel

object EnrollmentPeriodModelFactory {

    fun build(
        id: Int = 1,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now().plusYears(1),
        coverageStartDate: LocalDate = LocalDate.now(),
        coverageEndDate: LocalDate = LocalDate.now().plusYears(1),
        administrativeDivisionId: Int = 1,
        clock: Clock
    ) : EnrollmentPeriodModel {
        val now = Instant.now(clock)
        return EnrollmentPeriodModel(
            id = id,
            createdAt = createdAt ?: now,
            updatedAt = updatedAt ?: now,
            startDate = startDate,
            endDate = endDate,
            coverageStartDate = coverageStartDate,
            coverageEndDate = coverageEndDate,
            administrativeDivisionId = administrativeDivisionId
        )
    }

    fun create(
        enrollmentPeriodDao: EnrollmentPeriodDao,
        id: Int = 1,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now().plusYears(1),
        coverageStartDate: LocalDate = LocalDate.now(),
        coverageEndDate: LocalDate = LocalDate.now().plusYears(1),
        administrativeDivisionId: Int = 1,
        clock: Clock
    ) : EnrollmentPeriodModel {
        val model = build(
            id = id,
            createdAt = createdAt,
            updatedAt = updatedAt,
            clock = clock
        )
        enrollmentPeriodDao.insert(model)
        return model
    }
}
