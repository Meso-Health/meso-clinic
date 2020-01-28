package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.domain.utils.Age
import org.watsi.domain.utils.AgeUnit
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R
import java.util.UUID
import javax.inject.Inject

class MemberInformationViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val createMemberUseCase: CreateMemberUseCase,
    private val createIdentificationEventUseCase: CreateIdentificationEventUseCase,
    private val createEncounterUseCase: CreateEncounterUseCase,
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

    fun onVisitReasonChange(visitReason: Encounter.VisitReason?) {
        observable.value?.let { it ->
            val errors = it.errors.filterNot { it.key == VISIT_REASON_ERROR }
            observable.value = it.copy(visitReason = visitReason, errors = errors)
        }
    }

    fun onInboundReferralDateChange(inboundReferralDate: LocalDate) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(inboundReferralDate = inboundReferralDate)
        }
    }

    fun onFollowUpDateChange(followUpDate: LocalDate) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(followUpDate = followUpDate)
        }
    }

    private fun createMember(member: Member): Completable {
        return createMemberUseCase.execute(member, submitNow = true)
    }

    private fun createIdentificationEvent(idEventId: UUID, member: Member): Completable {
        val idEvent = IdentificationEvent(
            id = idEventId,
            memberId = member.id,
            occurredAt = clock.instant(),
            searchMethod = IdentificationEvent.SearchMethod.MANUAL_ENTRY,
            throughMemberId = null,
            clinicNumber = null,
            clinicNumberType = null
        )

        return createIdentificationEventUseCase.execute(idEvent)
    }

    private fun createPartialEncounter(idEventId: UUID, member: Member, visitReason: Encounter.VisitReason?, inboundReferralDate: LocalDate?): Completable {
        val encounter = Encounter(
            id = UUID.randomUUID(),
            memberId = member.id,
            identificationEventId = idEventId,
            occurredAt = clock.instant(),
            patientOutcome = null,
            visitReason = visitReason,
            inboundReferralDate = inboundReferralDate
        )
        val encounterWithExtras = EncounterWithExtras(
            encounter = encounter,
            encounterItemRelations = emptyList(),
            encounterForms = emptyList(),
            referral = null,
            member = member,
            diagnoses = emptyList()
        )

        return createEncounterUseCase.execute(encounterWithExtras, true, true, clock)
    }

    fun createAndCheckInMember(membershipNumber: String): Completable {
        val viewState = observable.value ?: return Completable.never()

        val validationErrors = FormValidator.formValidationErrors(viewState, sessionManager)
        if (validationErrors.isNotEmpty()) {
            observable.value = viewState.copy(errors = validationErrors)
            return Completable.error(ValidationException("Some fields are missing", validationErrors))
        }

        val member = toMember(viewState, membershipNumber, clock, sessionManager)
        val idEventId = UUID.randomUUID()
        return Completable.fromAction {
            createMember(member).blockingAwait()
            createIdentificationEvent(idEventId, member).blockingAwait()
            if (sessionManager.userHasPermission(SessionManager.Permissions.SYNC_PARTIAL_CLAIMS)) {
                var inboundReferralDate: LocalDate? = null
                if (sessionManager.userHasPermission(SessionManager.Permissions.CAPTURE_INBOUND_ENCOUNTER_INFORMATION)) {
                    inboundReferralDate = when (viewState.visitReason) {
                        Encounter.VisitReason.REFERRAL -> viewState.inboundReferralDate
                        Encounter.VisitReason.FOLLOW_UP -> viewState.followUpDate
                        else -> null
                    }
                }
                createPartialEncounter(idEventId, member, viewState.visitReason, inboundReferralDate).blockingAwait()
            }
        }.observeOn(AndroidSchedulers.mainThread())
    }

    data class ValidationException(val msg: String, val errors: Map<String, Int>): Exception(msg)

    companion object {
        const val MEMBER_GENDER_ERROR = "member_gender_error"
        const val MEMBER_NAME_ERROR = "member_age_name"
        const val MEMBER_AGE_ERROR = "member_age_error"
        const val MEMBER_MEDICAL_RECORD_NUMBER_ERROR = "member_medical_record_number_error"
        const val MEMBER_MEDICAL_RECORD_VALIDATION_ERROR = "member_medical_record_number_validation_error"
        const val VISIT_REASON_ERROR = "visit_reason_error"

        fun toMember(viewState: ViewState, membershipNumber: String, clock: Clock, sessionManager: SessionManager):
                Member {
            if (FormValidator.formValidationErrors(viewState, sessionManager).isEmpty() && viewState.gender != null
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
                    cardId = null,
                    householdId = null,
                    language = null,
                    phoneNumber = null,
                    photoUrl = null,
                    membershipNumber = membershipNumber,
                    medicalRecordNumber = viewState.medicalRecordNumber,
                    needsRenewal = null,
                    relationshipToHead = null,
                    archivedAt = null,
                    archivedReason = null,
                    renewedAt = null,
                    coverageEndDate = null
                )
            } else {
                throw IllegalStateException("MemberInformationViewModel.toMember should only be called with a valid viewState. " + viewState.toString())
            }
        }
    }

    object FormValidator {
        fun formValidationErrors(viewState: ViewState, sessionManager: SessionManager): Map<String, Int> {
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
            } else if (
                    !Member.isValidMedicalRecordNumber(
                        medicalRecordNumber = viewState.medicalRecordNumber,
                        minLength = BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MIN_LENGTH,
                        maxLength = BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MAX_LENGTH
                    )
            ) {
                errors[MEMBER_MEDICAL_RECORD_VALIDATION_ERROR] = R.string.medical_record_number_length_validation_error
            }

            if (sessionManager.userHasPermission(SessionManager.Permissions.CAPTURE_INBOUND_ENCOUNTER_INFORMATION)) {
                if (viewState.visitReason == null) {
                    errors[VISIT_REASON_ERROR] = R.string.missing_visit_reason
                }
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
        val visitReason: Encounter.VisitReason? = null,
        val inboundReferralDate: LocalDate? = null,
        val followUpDate: LocalDate? = null,
        val errors: Map<String, Int> = emptyMap()
    )
}
