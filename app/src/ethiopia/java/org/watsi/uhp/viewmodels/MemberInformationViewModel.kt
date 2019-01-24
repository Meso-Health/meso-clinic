package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.domain.utils.Age
import org.watsi.domain.utils.AgeUnit
import org.watsi.uhp.R
import java.util.UUID
import javax.inject.Inject

class MemberInformationViewModel @Inject constructor(
    private val createMemberUseCase: CreateMemberUseCase,
    private val createIdentificationEventUseCase: CreateIdentificationEventUseCase,
    private val clock: Clock
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    init {
        observable.value = ViewState()
    }

    fun getObservable(): LiveData<ViewState> {
        return observable
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

    fun onNameChange(name: String?) {
        observable.value?.let {
            if (name != it.name) {
                val errors = it.errors.filterNot { it.key == MEMBER_NAME_ERROR}
                observable.value = it.copy(name = name, errors = errors)
            }
        }
    }

    fun getName(): String {
        return observable.value?.name ?: ""
    }

    fun onMedicalRecordNumberChange(medicalRecordNumber: String?) {
        observable.value?.let {
            if (medicalRecordNumber != it.medicalRecordNumber) {
                val errors = it.errors.filterNot { it.key == MEMBER_MEDICAL_RECORD_NUMBER_ERROR}
                observable.value = it.copy(medicalRecordNumber = medicalRecordNumber, errors = errors)
            }
        }
    }

    fun createAndCheckInMember(membershipNumber: String): Completable {
        val viewState = observable.value ?: return Completable.never()

        val validationErrors = FormValidator.formValidationErrors(viewState)
        if (validationErrors.isNotEmpty()) {
            observable.value = viewState.copy(errors = validationErrors)
            return Completable.error(ValidationException("Some fields are missing", validationErrors))
        }

        val member = toMember(viewState, membershipNumber, clock)
        val idEvent = IdentificationEvent(
            id = UUID.randomUUID(),
            memberId = member.id,
            occurredAt = clock.instant(),
            searchMethod = IdentificationEvent.SearchMethod.MANUAL_ENTRY,
            throughMemberId = null,
            clinicNumber = null,
            clinicNumberType = null,
            fingerprintsVerificationTier = null,
            fingerprintsVerificationConfidence = null,
            fingerprintsVerificationResultCode = null
        )

        return Completable.concatArray(
            createMemberUseCase.execute(member, submitNow = true),
            createIdentificationEventUseCase.execute(idEvent)
        ).observeOn(AndroidSchedulers.mainThread())
    }

    data class ValidationException(val msg: String, val errors: Map<String, Int>): Exception(msg)

    companion object {
        const val MEMBER_GENDER_ERROR = "member_gender_error"
        const val MEMBER_NAME_ERROR = "member_age_name"
        const val MEMBER_AGE_ERROR = "member_age_error"
        const val MEMBER_MEDICAL_RECORD_NUMBER_ERROR = "member_medical_record_number_error"

        fun toMember(viewState: ViewState, membershipNumber: String, clock: Clock): Member {
            if (FormValidator.formValidationErrors(viewState).isEmpty() && viewState.gender != null
                    && viewState.name != null && viewState.age != null &&
                    viewState.medicalRecordNumber != null) {
                val birthdateWithAccuracy = Age(viewState.age, viewState.ageUnit).toBirthdateWithAccuracy()
                return Member(
                    id = UUID.randomUUID(),
                    name = viewState.name,
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
                    membershipNumber = membershipNumber,
                    medicalRecordNumber = viewState.medicalRecordNumber,
                    needsRenewal = null
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

            if (viewState.name == null || viewState.name.isBlank()) {
                errors[MEMBER_NAME_ERROR] = R.string.name_validation_error
            } else if (!Member.isValidName(viewState.name)) {
                errors[MEMBER_NAME_ERROR] = R.string.three_names_validation_error
            }

            if (viewState.age == null) {
                errors[MEMBER_AGE_ERROR] = R.string.age_validation_error
            } else if (viewState.age > Member.MAX_AGE) { // Use same limit for years and months for now
                errors[MEMBER_AGE_ERROR] = R.string.age_limit_validation_error
            }

            if (viewState.medicalRecordNumber == null) {
                errors[MEMBER_MEDICAL_RECORD_NUMBER_ERROR] = R.string.medical_record_number_validation_error
            } else if (!Member.isValidMedicalRecordNumber(viewState.medicalRecordNumber)) {
                errors[MEMBER_MEDICAL_RECORD_NUMBER_ERROR] = R.string.medical_record_number_length_validation_error
            }

            return errors
        }
    }

    data class ViewState(
        val gender: Member.Gender? = null,
        val name: String? = null,
        val age: Int? = null,
        val ageUnit: AgeUnit = AgeUnit.years,
        val medicalRecordNumber: String? = null,
        val errors: Map<String, Int> = emptyMap()
    )
}
