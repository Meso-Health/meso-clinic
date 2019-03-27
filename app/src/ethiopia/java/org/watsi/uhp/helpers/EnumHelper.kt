package org.watsi.uhp.helpers

import android.content.Context
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Encounter
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

    fun getPatientOutcomeChoicesMappings(): List<Pair<Encounter.PatientOutcome, Int>> {
        return listOf(
            Pair(Encounter.PatientOutcome.CURED_OR_DISCHARGED, R.string.outcome_cured_or_discharged),
            Pair(Encounter.PatientOutcome.REFERRED, R.string.outcome_referred),
            Pair(Encounter.PatientOutcome.FOLLOW_UP, R.string.outcome_follow_up),
            Pair(Encounter.PatientOutcome.REFUSED_SERVICE, R.string.outcome_refused_service),
            Pair(Encounter.PatientOutcome.EXPIRED, R.string.outcome_expired),
            Pair(Encounter.PatientOutcome.OTHER, R.string.outcome_other)
        )
    }

    fun patientOutcomeToDisplayedString(outcome: Encounter.PatientOutcome, context: Context, logger: Logger): String {
        val patientOutcomeChoicesMappings = getPatientOutcomeChoicesMappings()
        val patientOutcomePair = patientOutcomeChoicesMappings.find { pair -> pair.first == outcome }
        return if (patientOutcomePair != null) {
            context.getString(patientOutcomePair.second)
        } else {
            logger.error("Unable to find string that corresponds to $outcome in $patientOutcomeChoicesMappings")
            context.getString(R.string.other) // Just to be safe and not crash the app.
        }
    }

    fun getReceivingFacilityMappings(context: Context): List<Pair<String, String>> {
        val arrayEnglish = context.resources.getStringArray(R.array.receiving_facilities_stored_in_db)
        val arrayLocal = context.resources.getStringArray(R.array.receiving_facilities_translated)
        return (arrayEnglish zip arrayLocal).sortedBy { it.first }
    }

    fun receivingFacilityToDisplayedString(facilityName: String, context: Context): String {
        val mappings = getReceivingFacilityMappings(context)
        val facilityPair = mappings.find { pair -> pair.first == facilityName}
        return if (facilityPair != null) {
            facilityPair.second
        } else {
            // This is a custom name.
            facilityName
        }
    }
}
