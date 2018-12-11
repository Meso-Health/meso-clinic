package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.IsMemberCheckedInUseCase
import org.watsi.domain.usecases.LoadMemberUseCase
import org.watsi.domain.usecases.UpdateMemberUseCase
import java.util.UUID
import javax.inject.Inject

class EditMemberViewModel @Inject constructor(
    private val loadMemberUseCase: LoadMemberUseCase,
    private val updateMemberUseCase: UpdateMemberUseCase,
    private val isMemberCheckedInUseCase: IsMemberCheckedInUseCase
) : ViewModel() {

    val liveData = MediatorLiveData<ViewState>()

    fun getObservable(member: Member): LiveData<ViewState> {
        liveData.value = ViewState()
        liveData.addSource(LiveDataReactiveStreams.fromPublisher(loadMemberUseCase.execute(member.id)), {
            liveData.value = liveData.value?.copy(memberWithThumbnail = it)
        })
        liveData.addSource(LiveDataReactiveStreams.fromPublisher(isMemberCheckedInUseCase.execute(member.id)), {
            liveData.value = liveData.value?.copy(isCheckedIn = it)
        })
        return liveData
    }

    fun updateMedicalRecordNumber(medicalRecordNumberString: String): Completable {
        return liveData.value?.memberWithThumbnail?.member?.let {
            val medicalRecordNumber = if (medicalRecordNumberString.isBlank()) null else medicalRecordNumberString
            updateMemberUseCase.execute(it.copy(medicalRecordNumber = medicalRecordNumber))
                    .observeOn(AndroidSchedulers.mainThread())
        } ?: Completable.complete()
    }

    fun updatePhoto(rawPhotoId: UUID, thumbnailPhotoId: UUID): Completable {
        return liveData.value?.memberWithThumbnail?.member?.let {
            updateMemberUseCase.execute(it.copy(
                photoId = rawPhotoId,
                thumbnailPhotoId = thumbnailPhotoId)
            ).observeOn(AndroidSchedulers.mainThread())
        } ?: Completable.complete()
    }

    data class ViewState(
        val memberWithThumbnail: MemberWithThumbnail? = null,
        val isCheckedIn: Boolean? = null
    )
}
