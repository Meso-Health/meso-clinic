package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.domain.usecases.DismissMemberUseCase
import org.watsi.domain.usecases.LoadMemberUseCase
import org.watsi.domain.usecases.ShouldEnrollUseCase
import org.watsi.domain.usecases.UpdateMemberUseCase
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R
import java.util.UUID
import javax.inject.Inject

class EditMemberViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val loadMemberUseCase: LoadMemberUseCase,
    private val updateMemberUseCase: UpdateMemberUseCase,
    private val dismissMemberUseCase: DismissMemberUseCase,
    private val createIdentificationEventUseCase: CreateIdentificationEventUseCase,
    private val createEncounterUseCase: CreateEncounterUseCase,
    private val shouldEnrollUseCase: ShouldEnrollUseCase,
    private val clock: Clock
) : ViewModel() {

    val observable = MediatorLiveData<ViewState>()

    fun getObservable(member: Member): LiveData<ViewState> {
        observable.value = ViewState()
        observable.addSource(LiveDataReactiveStreams.fromPublisher(loadMemberUseCase.execute(member.id))) {
            observable.value = observable.value?.copy(memberWithThumbnail = it)
        }
        shouldEnrollUseCase.execute(member).subscribe { canRenew ->
            observable.postValue(observable.value?.copy(canRenew = canRenew))
        }
        return observable
    }

    fun getMember(): Member? {
        return observable.value?.memberWithThumbnail?.member
    }

    fun updateMedicalRecordNumber(medicalRecordNumberString: String): Completable {
        return observable.value?.let { viewState ->
            val validationErrors = viewState.validationErrors.filterNot { it.key == MEDICAL_RECORD_NUMBER_ERROR }
            observable.value = viewState.copy(validationErrors = validationErrors)

            viewState.memberWithThumbnail?.member?.let {
                val medicalRecordNumber = if (medicalRecordNumberString.isBlank()) null else medicalRecordNumberString
                updateMemberUseCase.execute(it.copy(medicalRecordNumber = medicalRecordNumber))
                        .observeOn(AndroidSchedulers.mainThread())
            } ?: Completable.complete()
        } ?: Completable.complete()
    }

    fun validateMedicalRecordNumber(medicalRecordNumberString: String?, errorString: String): String? {
        return if (medicalRecordNumberString == null ||
            Member.isValidMedicalRecordNumber(
                medicalRecordNumber = medicalRecordNumberString,
                minLength = BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MIN_LENGTH,
                maxLength = BuildConfig.MEMBER_MEDICAL_RECORD_NUMBER_MAX_LENGTH
            )
        ) {
            null
        } else {
            errorString
        }
    }

    fun onVisitReasonChange(visitReason: Encounter.VisitReason?) {
        observable.value?.let { viewState ->
            val validationErrors = viewState.validationErrors.filterNot { it.key == VISIT_REASON_ERROR }
            observable.value = viewState.copy(visitReason = visitReason, validationErrors = validationErrors)
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

    fun updatePhoto(rawPhotoId: UUID, thumbnailPhotoId: UUID): Completable {
        return observable.value?.memberWithThumbnail?.member?.let {
            updateMemberUseCase.execute(it.copy(
                photoId = rawPhotoId,
                thumbnailPhotoId = thumbnailPhotoId)
            ).observeOn(AndroidSchedulers.mainThread())
        } ?: Completable.complete()
    }

    fun dismissIdentificationEvent(identificationEventId: UUID): Completable =
            dismissMemberUseCase.execute(identificationEventId)

    object FormValidator {
        fun validateViewState(viewState: ViewState, sessionManager: SessionManager): Map<String, Int> {
            val errors = HashMap<String, Int>()
            if (viewState.memberWithThumbnail?.member?.medicalRecordNumber == null) {
                errors[EditMemberViewModel.MEDICAL_RECORD_NUMBER_ERROR] = R.string.missing_medical_record_number
            }
            if (sessionManager.userHasPermission(SessionManager.Permissions.CAPTURE_INBOUND_ENCOUNTER_INFORMATION)) {
                if (viewState.visitReason == null) {
                    errors[EditMemberViewModel.VISIT_REASON_ERROR] = R.string.missing_visit_reason
                }
            }
            return errors
        }
    }

    private fun createIdentificationEvent(idEventId: UUID, searchMethod: IdentificationEvent.SearchMethod): Completable {
        val member = getMember() ?: return Completable.never()
        val idEvent = IdentificationEvent(
            id = idEventId,
            memberId = member.id,
            occurredAt = clock.instant(),
            searchMethod = searchMethod,
            throughMemberId = null,
            clinicNumber = null,
            clinicNumberType = null
        )

        return createIdentificationEventUseCase.execute(idEvent)
    }

    private fun createPartialEncounter(idEventId: UUID, visitReason: Encounter.VisitReason?, inboundReferralDate: LocalDate?): Completable {
        val member = getMember() ?: return Completable.never()
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

    fun validateAndCheckInMember(searchMethod: IdentificationEvent.SearchMethod): Completable {
        val viewState = observable.value ?: return Completable.never()

        val validationErrors = FormValidator.validateViewState(viewState, sessionManager)
        if (validationErrors.isNotEmpty()) {
            observable.value = viewState.copy(validationErrors = validationErrors)
            return Completable.error(ValidationException("Some required check-in fields are missing", validationErrors))
        }

        val idEventId = UUID.randomUUID()
        return Completable.fromAction {
            createIdentificationEvent(idEventId, searchMethod).blockingAwait()
            if (sessionManager.userHasPermission(SessionManager.Permissions.SYNC_PARTIAL_CLAIMS)) {
                var inboundReferralDate: LocalDate? = null
                if (sessionManager.userHasPermission(SessionManager.Permissions.CAPTURE_INBOUND_ENCOUNTER_INFORMATION)) {
                    inboundReferralDate = when (viewState.visitReason) {
                        Encounter.VisitReason.REFERRAL -> viewState.inboundReferralDate
                        Encounter.VisitReason.FOLLOW_UP -> viewState.followUpDate
                        else -> null
                    }
                }
                createPartialEncounter(idEventId, viewState.visitReason, inboundReferralDate).blockingAwait()
            }
        }.observeOn(AndroidSchedulers.mainThread())
    }

    companion object {
        const val MEDICAL_RECORD_NUMBER_ERROR = "medical_record_number_error"
        const val VISIT_REASON_ERROR = "visit_reason_error"
    }

    data class ValidationException(val msg: String, val errors: Map<String, Int>): Exception(msg)

    data class ViewState(
        val memberWithThumbnail: MemberWithThumbnail? = null,
        val visitReason: Encounter.VisitReason? = null,
        val inboundReferralDate: LocalDate? = null,
        val followUpDate: LocalDate? = null,
        val validationErrors: Map<String, Int> = emptyMap(),
        val canRenew: Boolean = false
    )
}
