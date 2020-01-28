package org.watsi.uhp.helpers

import android.content.Context
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.Referral
import org.watsi.domain.entities.User
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R

object EnumHelper {
    fun getReferralReasonMappings(flavor: String = BuildConfig.FLAVOR): List<Pair<Referral.Reason, Int>> {
        return when (flavor) {
            "uganda" -> {
                listOf(
                    Pair(Referral.Reason.STOCKOUT, R.string.general_stockout),
                    Pair(Referral.Reason.INVESTIGATIVE_TESTS, R.string.investigative_tests),
                    Pair(Referral.Reason.ADDITIONAL_SERVICES, R.string.additional_services),
                    Pair(Referral.Reason.INSUFFICIENT_EQUIPMENT, R.string.insufficient_equipment),
                    Pair(Referral.Reason.SURGERY, R.string.surgery),
                    Pair(Referral.Reason.OTHER, R.string.other)
                )
            }
            "demo" -> {
                listOf(
                    Pair(Referral.Reason.FURTHER_CONSULTATION, R.string.further_consultation),
                    Pair(Referral.Reason.DRUG_STOCKOUT, R.string.drug_stockout),
                    Pair(Referral.Reason.INVESTIGATIVE_TESTS, R.string.investigative_tests),
                    Pair(Referral.Reason.INPATIENT_CARE, R.string.inpatient_care),
                    Pair(Referral.Reason.BED_SHORTAGE, R.string.bed_shortage),
                    Pair(Referral.Reason.FOLLOW_UP, R.string.follow_up),
                    Pair(Referral.Reason.OTHER, R.string.other)
                )
            }
            else -> {
                throw IllegalStateException("getReferralReasonMapping called when BuildConfig.FLAVOR is not ethiopia or uganda.")
            }
        }
    }

    fun referralReasonToDisplayedString(reason: Referral.Reason, context: Context, logger: Logger): String {
        val referralReasonMappings = getReferralReasonMappings()
        val referralReasonPair = referralReasonMappings.find { pair -> pair.first == reason }
        return if (referralReasonPair != null) {
            context.getString(referralReasonPair.second)
        } else {
            logger.error("Unable to find string that corresponds to $reason in $referralReasonMappings")
            context.getString(R.string.other) // Just to be safe and not crash the app.
        }
    }

    fun getPatientOutcomeMappings(): List<Pair<Encounter.PatientOutcome, Int>> {
        return listOf(
            Pair(Encounter.PatientOutcome.CURED_OR_DISCHARGED, R.string.outcome_cured_or_discharged),
            Pair(Encounter.PatientOutcome.REFERRED, R.string.outcome_referred),
            Pair(Encounter.PatientOutcome.FOLLOW_UP, R.string.outcome_follow_up),
            Pair(Encounter.PatientOutcome.REFUSED_SERVICE, R.string.outcome_refused_service),
            Pair(Encounter.PatientOutcome.DECEASED, R.string.outcome_expired),
            Pair(Encounter.PatientOutcome.OTHER, R.string.outcome_other)
        )
    }

    fun patientOutcomeToDisplayedString(outcome: Encounter.PatientOutcome, context: Context, logger: Logger): String {
        val patientOutcomeMappings = getPatientOutcomeMappings()
        val patientOutcomePair = patientOutcomeMappings.find { pair -> pair.first == outcome }
        return if (patientOutcomePair != null) {
            context.getString(patientOutcomePair.second)
        } else {
            logger.error("Unable to find string that corresponds to $outcome in $patientOutcomeMappings")
            context.getString(R.string.other) // Just to be safe and not crash the app.
        }
    }
    
    fun getVisitReasonMappings(providerType: User.ProviderType?, logger: Logger): List<Pair<Encounter.VisitReason, Int>> {
        if (providerType == User.ProviderType.PRIMARY_HOSPITAL) {
            return listOf(
                Pair(Encounter.VisitReason.REFERRAL, R.string.visit_reason_referral),
                Pair(Encounter.VisitReason.NO_REFERRAL, R.string.visit_reason_no_referral),
                Pair(Encounter.VisitReason.SELF_REFERRAL, R.string.visit_reason_self_referral),
                Pair(Encounter.VisitReason.FOLLOW_UP, R.string.visit_reason_follow_up),
                Pair(Encounter.VisitReason.EMERGENCY, R.string.visit_reason_emergency)
            )
        } else if (providerType == User.ProviderType.GENERAL_HOSPITAL || providerType == User.ProviderType.TERTIARY_HOSPITAL) {
            return listOf(
                Pair(Encounter.VisitReason.REFERRAL, R.string.visit_reason_referral),
                Pair(Encounter.VisitReason.SELF_REFERRAL, R.string.visit_reason_self_referral),
                Pair(Encounter.VisitReason.FOLLOW_UP, R.string.visit_reason_follow_up),
                Pair(Encounter.VisitReason.EMERGENCY, R.string.visit_reason_emergency)
            )
        } else {
            logger.error("getVisitReasonMappings() called for a provider type that is not a hospital: $providerType")
            return emptyList()
        }
    }
}
