package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Single
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Member
import org.watsi.domain.utils.Age
import org.watsi.domain.utils.AgeUnit
import org.watsi.uhp.R
import org.watsi.uhp.flowstates.EncounterFlowState
import java.util.UUID
import javax.inject.Inject

class MemberInformationViewModel @Inject constructor(private val clock: Clock) : ViewModel() {
    private val observable = MutableLiveData<ViewState>()

    fun getObservable(encounter: EncounterFlowState?, membershipNumber: String?): LiveData<ViewState> {
        encounter?.let {
            return getObservableWithEncounter(it)
        }

        membershipNumber?.let {
            return getObservableWithMembershipNumber(it)
        }

        throw IllegalStateException("Observable requires either encounter or membershipNumber")
    }

    private fun getObservableWithMembershipNumber(membershipNumber: String): LiveData<ViewState> {
        observable.value = ViewState(membershipNumber = membershipNumber)
        return observable
    }

    private fun getObservableWithEncounter(encounter: EncounterFlowState): LiveData<ViewState> {
        encounter.member?.let {
            if (it.membershipNumber == null) {
                throw IllegalStateException("Membership Number should not be null")
            }

            val membershipNumber = it.membershipNumber ?: ""
            val age = when (it.birthdateAccuracy) {
                Member.DateAccuracy.Y -> it.getAgeYears(clock)
                Member.DateAccuracy.M -> it.getAgeMonths(clock)
                Member.DateAccuracy.D -> it.getAgeDays(clock)
            }
            val ageUnit = getInitialAgeUnit(encounter)
            val gender = it.gender
            val medicalRecordNumber = it.medicalRecordNumber

            observable.value = ViewState(membershipNumber = membershipNumber, age = age, ageUnit = ageUnit, gender = gender, medicalRecordNumber = medicalRecordNumber)
            return observable
        }

        throw IllegalStateException("No member on encounter")
    }

    fun getInitialAgeUnit(encounter: EncounterFlowState): AgeUnit {
        encounter.member?.let {
            when (it.birthdateAccuracy) {
                Member.DateAccuracy.Y -> return AgeUnit.years
                Member.DateAccuracy.M -> return AgeUnit.months
                Member.DateAccuracy.D -> return AgeUnit.days
            }
        }
        return AgeUnit.years
    }

    fun onAgeChange(age: Int?) {
        observable.value?.let {
            if (age != it.age) {
                val errors = it.errors.filterNot { it.key == MEMBER_AGE_ERROR }
                observable.value = it.copy(age = age, errors = errors)
            }
        }
    }

    fun onAgeUnitChange(ageUnit: AgeUnit) {
        observable.value?.let { observable.value = it.copy(ageUnit = ageUnit) }
    }

    fun onGenderChange(gender: Member.Gender) {
        observable.value?.let {
            val errors = it.errors.filterNot { it.key == MEMBER_GENDER_ERROR }
            observable.value = it.copy(gender = gender, errors = errors)
        }
    }

    fun onMedicalRecordNumberChange(medicalRecordNumber: String?) {
        observable.value?.let {
            if (medicalRecordNumber != it.medicalRecordNumber) {
                val errors = it.errors.filterNot { it.key == MEMBER_MEDICAL_RECORD_NUMBER_ERROR}
                observable.value = it.copy(medicalRecordNumber = medicalRecordNumber, errors = errors)
            }
        }
    }

    fun updateEncounterWithMember(encounterFlowState: EncounterFlowState): Single<EncounterFlowState> {
        return Single.fromCallable {
            val viewState = observable.value
            if (viewState == null) {
                throw IllegalStateException("MemberInformationViewModel.buildEncounterFlow should not be called with a null viewState.")
            }

            val validationErrors = FormValidator.formValidationErrors(viewState)
            if (validationErrors.isNotEmpty()) {
                observable.value = viewState.copy(errors = validationErrors)
                throw ValidationException("Some fields are missing", validationErrors)
            } else {
                encounterFlowState.member = toMember(viewState, encounterFlowState.encounter.memberId, clock)
                encounterFlowState
            }
        }
    }

    data class ValidationException(val msg: String, val errors: Map<String, Int>): Exception(msg)

    companion object {
        const val MEMBER_AGE_ERROR = "member_age_error"
        const val MEMBER_GENDER_ERROR = "member_gender_error"
        const val MEMBER_MEDICAL_RECORD_NUMBER_ERROR = "member_medical_record_number_error"

        fun toMember(viewState: ViewState, memberId: UUID, clock: Clock): Member {
            if (FormValidator.formValidationErrors(viewState).isEmpty() && viewState.gender != null
                    && viewState.age != null && viewState.medicalRecordNumber != null) {
                val birthdateWithAccuracy = Age(viewState.age, viewState.ageUnit).toBirthdateWithAccuracy()
                return Member(
                    id = memberId,
                    name = "Member Name", // Placeholder until we make a platform decision that "Member" doesn't require a name.
                    enrolledAt = Instant.now(clock),
                    birthdate = birthdateWithAccuracy.first,
                    birthdateAccuracy = birthdateWithAccuracy.second,
                    gender = viewState.gender,
                    photoId = null,
                    thumbnailPhotoId = null,
                    fingerprintsGuid = null,
                    cardId = null,
                    householdId = null,
                    language = null,
                    phoneNumber = null,
                    photoUrl = null,
                    membershipNumber = viewState.membershipNumber,
                    medicalRecordNumber = viewState.medicalRecordNumber
                )
            } else {
                throw IllegalStateException("MemberInformationViewModel.toMember should only be called with a valid viewState. " + viewState.toString())
            }
        }
    }


    object FormValidator {
        fun formValidationErrors(viewState: ViewState): Map<String, Int> {
            val errors = HashMap<String, Int>()

            if (viewState.gender == null) {
                errors[MEMBER_GENDER_ERROR] = R.string.gender_validation_error
            }

            if (viewState.age == null) {
                errors[MEMBER_AGE_ERROR] = R.string.age_validation_error
            }

            if (viewState.medicalRecordNumber.isNullOrBlank()) {
                errors[MEMBER_MEDICAL_RECORD_NUMBER_ERROR] = R.string.medical_record_number_validation_error
            }

            return errors
        }
    }

    data class ViewState(val membershipNumber: String,
                         val age: Int? = null,
                         val ageUnit: AgeUnit = AgeUnit.years,
                         val gender: Member.Gender? = null,
                         val medicalRecordNumber: String? = null,
                         val errors: Map<String, Int> = emptyMap())
}
