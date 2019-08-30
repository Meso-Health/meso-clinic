package org.watsi.device.api.models

import org.threeten.bp.LocalDate
import org.watsi.domain.entities.EnrollmentPeriod

data class EnrollmentPeriodApi(
    val id: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val coverageStartDate: LocalDate,
    val coverageEndDate: LocalDate,
    val administrativeDivisionId: Int
) {
    fun toEnrollmentPeriod(): EnrollmentPeriod {
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
