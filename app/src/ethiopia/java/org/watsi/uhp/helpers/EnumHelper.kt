package org.watsi.uhp.helpers

import android.content.Context
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Referral
import org.watsi.uhp.R

object EnumHelper {
    fun getReasonChoicesMappings(): List<Pair<Referral.Reason, Int>> {
        return listOf(
            Pair(Referral.Reason.FURTHER_CONSULTATION, R.string.further_consultation),
            Pair(Referral.Reason.DRUG_STOCKOUT, R.string.drug_stockout),
            Pair(Referral.Reason.INVESTIGATIVE_TESTS, R.string.investigative_tests),
            Pair(Referral.Reason.INPATIENT_CARE, R.string.inpatient_care),
            Pair(Referral.Reason.BED_SHORTAGE, R.string.bed_shortage),
            Pair(Referral.Reason.FOLLOW_UP, R.string.follow_up),
            Pair(Referral.Reason.OTHER, R.string.other)
        )
    }

    fun referralReasonToDisplayedString(reason: Referral.Reason, context: Context, logger: Logger): String {
        val reasonChoicesMappings = getReasonChoicesMappings()
        val reasonPair = reasonChoicesMappings.find { pair -> pair.first == reason }
        return if (reasonPair != null) {
            context.getString(reasonPair.second)
        } else {
            logger.error("Unable to find string that corresponds to $reason in $reasonChoicesMappings")
            context.getString(R.string.other) // Just to be safe and not crash the app.
        }
    }
}
