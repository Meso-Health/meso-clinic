package org.watsi.domain.factories

import org.threeten.bp.LocalDate
import org.watsi.domain.entities.EnrollmentPeriod

object EnrollmentPeriodFactory {

    fun build(
        id: Int = 1,
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now().plusYears(1),
        coverageStartDate: LocalDate = LocalDate.now(),
        coverageEndDate: LocalDate = LocalDate.now().plusYears(1),
        administrativeDivisionId: Int = 1
    ) : EnrollmentPeriod {
        return EnrollmentPeriod(
            id = id,
            startDate = startDate,
            endDate = endDate,
            coverageStartDate = coverageStartDate,
            coverageEndDate = coverageEndDate,
            administrativeDivisionId = administrativeDivisionId
        )
    }
}
