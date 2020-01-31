package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.EnrollmentPeriod

@Entity(tableName = "enrollment_periods")
data class EnrollmentPeriodModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant,
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

    companion object {
        fun fromEnrollmentPeriod(enrollmentPeriod: EnrollmentPeriod, clock: Clock): EnrollmentPeriodModel {
            val now = clock.instant()
            return EnrollmentPeriodModel(
                id = enrollmentPeriod.id,
                createdAt = now,
                updatedAt = now,
                startDate = enrollmentPeriod.startDate,
                endDate = enrollmentPeriod.endDate,
                coverageStartDate = enrollmentPeriod.coverageStartDate,
                coverageEndDate = enrollmentPeriod.coverageEndDate,
                administrativeDivisionId = enrollmentPeriod.administrativeDivisionId
            )
        }
    }
}
