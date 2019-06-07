package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.User
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.CreateIdentificationEventUseCase
import org.watsi.domain.usecases.DismissMemberUseCase
import org.watsi.domain.usecases.IsMemberCheckedInUseCase
import org.watsi.domain.usecases.LoadMemberUseCase
import org.watsi.domain.usecases.UpdateMemberUseCase
import org.watsi.uhp.R
import java.util.UUID
import javax.inject.Inject

class EditMemberViewModel @Inject constructor(
    private val loadMemberUseCase: LoadMemberUseCase,
    private val updateMemberUseCase: UpdateMemberUseCase,
    private val isMemberCheckedInUseCase: IsMemberCheckedInUseCase,
    private val dismissMemberUseCase: DismissMemberUseCase,
    private val createIdentificationEventUseCase: CreateIdentificationEventUseCase,
    private val clock: Clock
) : ViewModel() {

    val observable = MediatorLiveData<ViewState>()

    fun getObservable(member: Member): LiveData<ViewState> {
        observable.value = ViewState()
        observable.addSource(LiveDataReactiveStreams.fromPublisher(loadMemberUseCase.execute(member.id))) {
            observable.value = observable.value?.copy(memberWithThumbnail = it)
        }
        observable.addSource(LiveDataReactiveStreams.fromPublisher(isMemberCheckedInUseCase.execute(member.id))) {
            observable.value = observable.value?.copy(isCheckedIn = it)
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
            Member.isValidMedicalRecordNumber(medicalRecordNumberString)) { null } else { errorString }
    }

    fun updateVisitReason(visitReason: Encounter.VisitReason?) {
        observable.value?.let { viewState ->
            val validationErrors = viewState.validationErrors.filterNot { it.key == VISIT_REASON_ERROR }
            observable.value = viewState.copy(visitReason = visitReason, validationErrors = validationErrors)
        }
    }

    fun updateInboundReferralDate(inboundReferralDate: LocalDate) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(inboundReferralDate = inboundReferralDate)
        }
    }

    fun updateFollowUpDate(followUpDate: LocalDate) {
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

    fun dismissIdentificationEvent(): Completable {
        return observable.value?.memberWithThumbnail?.member?.let { member ->
            dismissMemberUseCase.execute(member.id)
        } ?: Completable.error(IllegalStateException("Tried to dismiss an identificationEvent but member has not loaded yet"))
    }

    object FormValidator {
        fun validateViewState(viewState: ViewState, user: User): Map<String, Int> {
            val errors = HashMap<String, Int>()
            if (viewState.memberWithThumbnail?.member?.medicalRecordNumber == null) {
                errors[EditMemberViewModel.MEDICAL_RECORD_NUMBER_ERROR] = R.string.missing_medical_record_number
            }
            if (user.isHospital() && viewState.visitReason == null) {
                errors[EditMemberViewModel.VISIT_REASON_ERROR] = R.string.missing_visit_reason
            }
            return errors
        }
    }

    private fun createIdentificationEvent(searchMethod: IdentificationEvent.SearchMethod): Completable {
        return getMember()?.let {
            val idEvent = IdentificationEvent(
                id = UUID.randomUUID(),
                memberId = it.id,
                occurredAt = clock.instant(),
                searchMethod = searchMethod,
                throughMemberId = null,
                clinicNumber = null,
                clinicNumberType = null,
                fingerprintsVerificationTier = null,
                fingerprintsVerificationConfidence = null,
                fingerprintsVerificationResultCode = null
            )
            return createIdentificationEventUseCase.execute(idEvent)
        } ?: Completable.complete()
    }

    fun validateAndCheckInMember(searchMethod: IdentificationEvent.SearchMethod, user: User): Completable {
        return observable.value?.let { viewState ->
            val validationErrors = FormValidator.validateViewState(viewState, user)
            if (validationErrors.isNotEmpty()) {
                observable.value = viewState.copy(validationErrors = validationErrors)
                Completable.error(ValidationException("Some required check-in fields are missing", validationErrors))
            } else {
                createIdentificationEvent(searchMethod)
                // TODO: create partial encounter
            }
        } ?: Completable.never()
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
        val isCheckedIn: Boolean? = null,
        val validationErrors: Map<String, Int> = emptyMap()
    )
}
