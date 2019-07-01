package org.watsi.uhp.helpers

import android.content.Context
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Member.DateAccuracy
import org.watsi.domain.utils.AgeUnit
import org.watsi.domain.utils.DateUtils
import org.watsi.uhp.R

object StringHelper {
    fun truncateWithEllipses(qrCode: String, maxCharlength: Int = 25): String {
        return if (maxCharlength >= qrCode.length) {
            qrCode
        } else {
            qrCode.substring(0, maxCharlength) + "..."
        }
    }

    fun formatBirthdate(birthdate: LocalDate, accuracy: DateAccuracy, context: Context): String {
        return when (accuracy) {
            DateAccuracy.Y -> {
                "${DateUtils.getYearsAgo(birthdate)} ${context.getString(R.string.years)}"
            }
            DateAccuracy.M -> {
                "${DateUtils.getMonthsAgo(birthdate)} ${context.getString(R.string.months)}"
            }
            DateAccuracy.D -> {
                DateUtils.formatLocalDate(birthdate)
            }
        }
    }

    fun fromStringToAgeUnit(string: String, context: Context): AgeUnit {
        return when (string) {
            context.getString(R.string.years) -> {
                AgeUnit.years
            }
            context.getString(R.string.months) -> {
                AgeUnit.months
            }
            else -> {
                throw IllegalStateException("AgeUnitPresenter.fromStringToAgeUnit called with invalid string: $string")
            }
        }
    }

    fun getStringNullSafe(resourceId: Int?, context: Context): String? {
        return if (resourceId == null) {
            null
        } else {
            context.getString(resourceId)
        }
    }

    fun formatAgeAndGender(member: Member, context: Context, clock: Clock = Clock.systemUTC()): String {
        val genderString = if (member.gender == Member.Gender.F) {
            context.getString(R.string.female)
        } else {
            context.getString(R.string.male)
        }
        return "$genderString ${context.getString(R.string.middle_dot)} ${getDisplayAge(member, context, clock)}"
    }

    fun formatMembershipInfo(member: Member, context: Context): String {
        val includeSeparator = member.membershipNumber != null && member.medicalRecordNumber != null
        return "${member.membershipNumber ?: ""}${if (includeSeparator) " ${context.getString(R.string.middle_dot)} " else ""}${member.medicalRecordNumber ?: ""}"
    }

    /**
     * Returns quantity in days if under 1 month old, quantity in months if under 2 years old,
     * or in years otherwise, regardless of birthdate accuracy.
     */
    fun getDisplayAge(member: Member, context: Context, clock: Clock = Clock.systemUTC()): String {
        val ageYears = member.getAgeYears(clock)
        val ageMonths = member.getAgeMonths(clock)
        val ageDays = member.getAgeDays(clock)

        if (ageYears >= 2) {
            return "${ageYears} ${context.getString(R.string.years)}"
        } else if (ageMonths >= 1) {
            return "${ageMonths} ${context.getString(R.string.months)}"
        } else {
            return "${ageDays} ${context.getString(R.string.days)}"
        }
    }

    fun formatPhoneNumber(member: Member, context: Context): String? {
        val phoneNumber = member.phoneNumber
        return when (phoneNumber?.length) {
            10 -> "${context.getString(R.string.phone_number_prefix)} ${phoneNumber.substring(1, 4)} ${phoneNumber.substring(4, 7)} " +
                    "${phoneNumber.substring(7)}"
            9 -> "${context.getString(R.string.phone_number_prefix)} ${phoneNumber.substring(0, 3)} ${phoneNumber.substring(3, 6)} " +
                    "${phoneNumber.substring(6)}"
            else -> null
        }
    }
}
