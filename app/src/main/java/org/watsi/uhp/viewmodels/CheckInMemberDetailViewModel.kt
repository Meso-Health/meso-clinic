package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import org.watsi.domain.relations.MemberWithThumbnail
import org.watsi.domain.usecases.LoadMemberUseCase
import java.util.UUID
import javax.inject.Inject

class CheckInMemberDetailViewModel @Inject constructor(
        private val loadMemberUseCase: LoadMemberUseCase
) : ViewModel() {

    fun getObservable(memberId: UUID): LiveData<MemberWithThumbnail> {
        val transformedFlowable = loadMemberUseCase.execute(memberId).map { it }
        return LiveDataReactiveStreams.fromPublisher(transformedFlowable)
    }
}