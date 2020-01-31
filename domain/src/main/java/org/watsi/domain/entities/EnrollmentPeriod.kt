package org.watsi.domain.entities

import org.threeten.bp.LocalDate
import java.io.Serializable

data class EnrollmentPeriod(
    val id: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val coverageStartDate: LocalDate,
    val coverageEndDate: LocalDate,
    val administrativeDivisionId: Int
): Serializable
