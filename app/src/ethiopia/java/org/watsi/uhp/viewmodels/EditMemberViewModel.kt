package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.DismissMemberUseCase
import org.watsi.domain.usecases.IsMemberCheckedInUseCase
import org.watsi.domain.usecases.LoadMemberUseCase
import org.watsi.domain.usecases.UpdateMemberUseCase
import java.util.UUID
import javax.inject.Inject

class EditMemberViewModel @Inject constructor(
    private val loadMemberUseCase: LoadMemberUseCase,
    private val updateMemberUseCase: UpdateMemberUseCase,
    private val isMemberCheckedInUseCase: IsMemberCheckedInUseCase,
    private val dismissMemberUseCase: DismissMemberUseCase
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

    fun updateMedicalRecordNumber(medicalRecordNumberString: String): Completable {
        return observable.value?.memberWithThumbnail?.member?.let {
            val medicalRecordNumber = if (medicalRecordNumberString.isBlank()) null else medicalRecordNumberString
            updateMemberUseCase.execute(it.copy(medicalRecordNumber = medicalRecordNumber))
                    .observeOn(AndroidSchedulers.mainThread())
        } ?: Completable.complete()
    }

    fun validateMedicalRecordNumber(medicalRecordNumberString: String?, errorString: String): String? {
        return if (medicalRecordNumberString == null ||
            Member.isValidMedicalRecordNumber(medicalRecordNumberString)) { null } else { errorString }
    }

    fun updateVisitReason(visitReason: Encounter.VisitReason?) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(visitReason = visitReason)
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

    data class ViewState(
        val memberWithThumbnail: MemberWithThumbnail? = null,
        val visitReason: Encounter.VisitReason? = null,
        val inboundReferralDate: LocalDate? = null,
        val followUpDate: LocalDate? = null,
        val isCheckedIn: Boolean? = null
    )
}
