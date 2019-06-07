package org.watsi.uhp.helpers

import android.content.Context
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.Referral
import org.watsi.domain.entities.User
import org.watsi.uhp.R

object EnumHelper {
    fun getReferralReasonMappings(): List<Pair<Referral.Reason, Int>> {
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
    
    fun getVisitReasonMappings(): List<Pair<Encounter.VisitReason, Int>> {
        return listOf(
            Pair(Encounter.VisitReason.REFERRAL, R.string.visit_reason_referral),
            Pair(Encounter.VisitReason.NO_REFERRAL, R.string.visit_reason_no_referral),
            Pair(Encounter.VisitReason.SELF_REFERRAL, R.string.visit_reason_self_referral),
            Pair(Encounter.VisitReason.FOLLOW_UP, R.string.visit_reason_follow_up),
            Pair(Encounter.VisitReason.EMERGENCY, R.string.visit_reason_emergency)
        )
    }

    fun getProviderTypeMappings(): List<Pair<User.ProviderType, Int>> {
        return listOf(
            Pair(User.ProviderType.UNCLASSIFIED, R.string.provider_type_unclassified),
            Pair(User.ProviderType.HEALTH_CENTER, R.string.provider_type_clinic),
            Pair(User.ProviderType.PRIMARY_HOSPITAL, R.string.provider_type_primary_hospital),
            Pair(User.ProviderType.GENERAL_HOSPITAL, R.string.provider_type_general_hospital),
            Pair(User.ProviderType.TERTIARY_HOSPITAL, R.string.provider_type_tertiary_hospital)
        )
    }

    fun providerTypeToDisplayedString(type: User.ProviderType, context: Context, logger: Logger): String {
        val providerTypeMappings = getProviderTypeMappings()
        val providerTypePair = providerTypeMappings.find { pair -> pair.first == type }
        return if (providerTypePair != null) {
            context.getString(providerTypePair.second)
        } else {
            logger.error("Unable to find string that corresponds to $type in $providerTypeMappings")
            context.getString(R.string.other) // Just to be safe and not crash the app.
        }
    }
}
