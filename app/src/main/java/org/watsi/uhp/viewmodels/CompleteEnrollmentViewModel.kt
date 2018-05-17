package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.LoadMemberUseCase
import org.watsi.domain.usecases.UpdateMemberUseCase
import java.util.UUID
import javax.inject.Inject

class CompleteEnrollmentViewModel @Inject constructor(
        private val loadMemberUseCase: LoadMemberUseCase,
        private val updateMemberUseCase: UpdateMemberUseCase,
        private val logger: Logger
        ): ViewModel() {

    private lateinit var viewStateObservable: LiveData<MemberWithThumbnail?>

    fun getObservable(memberId: UUID): LiveData<MemberWithThumbnail?> {
        val transformedFlowable = loadMemberUseCase.execute(memberId)
                .onErrorReturn {
                    logger.error(it)
                    null
                }
        viewStateObservable = LiveDataReactiveStreams.fromPublisher(transformedFlowable)
        return viewStateObservable
    }

    internal fun callIfMemberExists(updateMember: (member: Member) -> Completable): Completable {
        return viewStateObservable.value?.member?.let { member ->
            updateMember(member)
        } ?: Completable.never()
    }

    fun updateFingerprints(fingerprintsId: UUID): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(fingerprintsGuid = fingerprintsId))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun updatePhoto(rawPhotoId: UUID, thumbnailPhotoId: UUID): Completable {
        return callIfMemberExists { member ->
            updateMemberUseCase.execute(member.copy(
                    photoId = rawPhotoId, thumbnailPhotoId = thumbnailPhotoId))
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}