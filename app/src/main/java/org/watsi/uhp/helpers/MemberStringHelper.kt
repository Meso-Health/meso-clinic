package org.watsi.uhp.helpers

import android.content.Context
import org.threeten.bp.Clock
import org.watsi.domain.entities.Member
import org.watsi.uhp.R
import org.watsi.uhp.R.string.days
import org.watsi.uhp.R.string.female
import org.watsi.uhp.R.string.male
import org.watsi.uhp.R.string.middle_dot
import org.watsi.uhp.R.string.months
import org.watsi.uhp.R.string.years

object MemberStringHelper {

    fun formatAgeAndGender(member: Member, context: Context, clock: Clock): String {
        val genderString = if (member.gender == Member.Gender.F) {
            context.getString(female)
        } else {
            context.getString(male)
        }
        return "$genderString ${context.getString(middle_dot)} ${getDisplayAge(member, context, clock)}"
    }

    fun formatMembershipInfo(member: Member, context: Context): String {
        val includeSeparator = member.membershipNumber != null && member.medicalRecordNumber != null
        return "${member.membershipNumber ?: ""}${if (includeSeparator) " ${context.getString(middle_dot)} " else ""}${member.medicalRecordNumber ?: ""}"
    }

    /**
     * Returns quantity in days if under 1 month old, quantity in months if under 2 years old,
     * or in years otherwise, regardless of birthdate accuracy.
     */
    fun getDisplayAge(member: Member, context: Context, clock: Clock): String {
        val ageYears = member.getAgeYears(clock)
        val ageMonths = member.getAgeMonths(clock)
        val ageDays = member.getAgeDays(clock)

        if (ageYears >= 2) {
            return "${ageYears} ${context.getString(years)}"
        } else if (ageMonths >= 1) {
            return "${ageMonths} ${context.getString(months)}"
        } else {
            return "${ageDays} ${context.getString(days)}"
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
